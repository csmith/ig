package com.greboid.scraper.retrievers

import com.greboid.scraper.Config
import com.greboid.scraper.Database
import com.greboid.scraper.Instagram
import com.greboid.scraper.PostType
import com.greboid.scraper.Retriever
import com.mortennobel.imagescaling.AdvancedResizeOp
import com.mortennobel.imagescaling.ResampleOp
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import java.io.File
import java.net.URL
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO

class IGRetriever(
        private val database: Database,
        private val config: Config,
        private val instagram: Instagram = Instagram()
) : Retriever {

    private val isActive = AtomicBoolean(false)
    val logger = KotlinLogging.logger {}

    override suspend fun start() {
        logger.info("Starting IG retriever")
        isActive.set(true)
        while (isActive.get()) {
            retrieveAll()
        }
        logger.info("IG retriever ended")
    }

    override suspend fun stop() {
        logger.info("Stopping IG retriever")
        isActive.set(false)
    }

    override suspend fun retrieveAll() {
        logger.info("Retrieving all users")
        val users = database.getUsers()
        val delay = (config.refreshDelay * 60) / users.size
        for (user in users) {
            retrieve(user)
            logger.trace("Sleeping for $delay before next user")
            delay(Duration.ofSeconds(delay.toLong()))
        }
    }

    override suspend fun retrieve(identifier: String) {
        logger.info("Retrieving: $identifier")
        val profile = instagram.getUserProfile(identifier) ?: return
        val userID = database.getUserID(identifier)
                ?: run { println("Unable to get id for user: ${profile.username}"); return }
        profile.posts.forEach { post ->
            if (post.type == PostType.SIDECAR) {
                post.displayURL.forEachIndexed { index, url ->
                    val out = File("thumbs/${post.shortcode}$index.jpg")
                    if (!out.exists()) {
                        thumbnail(url, out)
                    }
                    database.addIGPost(post.shortcode, index, userID, out.toString(),
                            url.toString(), post.caption, post.timestamp)
                }
            } else {
                val out = File("thumbs/${post.shortcode}.jpg")
                if (!out.exists()) {
                    thumbnail(post.thumbnailURL, out)
                }
                database.addIGPost(post.shortcode, 0, userID, out.toString(),
                        post.displayURL.first().toString(), post.caption, post.timestamp)
            }
        }
    }
}

private fun thumbnail(input: URL, output: File) {
    output.parentFile.mkdirs()
    val source = ImageIO.read(input)
    val widthRatio = 200.toDouble() / source.width
    val heightRatio = 200.toDouble() / source.height
    val ratio = Math.min(widthRatio, heightRatio)
    val resampleOp = ResampleOp((source.width * ratio).toInt(), (source.height * ratio).toInt())
    resampleOp.unsharpenMask = AdvancedResizeOp.UnsharpenMask.Normal
    val scaled = resampleOp.filter(source, null)
    ImageIO.write(scaled, "jpg", output)
}
