package com.greboid.scraper

import org.sqlite.SQLiteConfig
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File

fun main(args: Array<String>) {
    val config = getConfig("defaults.yml")
    File(config.db).delete()
    val sqliteconfig = SQLiteConfig()
    sqliteconfig.transactionMode = SQLiteConfig.TransactionMode.IMMEDIATE
    sqliteconfig.setJournalMode(SQLiteConfig.JournalMode.OFF)
    val conn = sqliteconfig.createConnection("jdbc:sqlite:" + config.db)
    conn.createStatement().executeUpdate(Schema.createAllTables)
    conn.autoCommit = false
    addProfiles(config.profiles, conn)
    pruneProfiles(config.profiles, conn)
}

class Config(var db: String = "", var profiles: Map<String, List<String>> = emptyMap())

fun getConfig(fileName: String) : Config {
    return Yaml(Constructor(Config::class.java))
            .load(File(fileName).reader()) as Config
}

fun examplegetConfig() {
    val config = getConfig("defaults.yml")
    println(config.db)
    println(config.profiles)
}