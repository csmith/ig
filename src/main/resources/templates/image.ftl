<div class="perfundo">
  <a class="perfundo__link" href="#${image.shortcode}${image.ord}">
    <img src="${image.thumb}" alt="${image.caption}">
  </a>
  <div id="${image.shortcode}${image.ord}" class="perfundo__overlay fadeInLeft">
    <figure class="perfundo__content perfundo__figure">
      <img src="${image.thumb}" alt="${image.caption}">
      <div class="perfundo__image" style="width: 800px; padding-top: 100%; background-image: url(${image.url}); background-size: contain; background-repeat: no-repeat;"></div>
      <figcaption class="perfundo__figcaption">${image.caption}</figcaption>
    </figure>
    <a href="#perfundo-untarget" class="perfundo__close perfundo__control">Close</a>
  </div>
</div>