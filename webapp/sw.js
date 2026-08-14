const CACHE_NAME = "ptp-360-v10";
const BASE_URL = new URL("./", self.location.href);
const cacheUrl = (path) => new URL(path, BASE_URL).pathname;

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) =>
      cache.addAll([
        cacheUrl("./"),
        cacheUrl("manifest.webmanifest"),
        cacheUrl("icon-192.png"),
        cacheUrl("ptp-splash-logo.png"),
        cacheUrl("ptp-home-banner.png"),
        cacheUrl("ptp-video-gallery.png"),
        cacheUrl("ptp-photo-gallery.png"),
        cacheUrl("ptp-about-us-button.png"),
        cacheUrl("ptp-videos-background.png"),
        cacheUrl("ptp-photos-background.png"),
        cacheUrl("ptp-about-background.png"),
        cacheUrl("ptp-walking-video-button-template.png"),
        cacheUrl("ptp-conversation-videos-category-button.png"),
        cacheUrl("ptp-ptp-videos-category-button.png"),
        cacheUrl("ptp-ptp-video-button-template.png"),
        cacheUrl("ptp-biblical-photo-button-template.png"),
        cacheUrl("ptp-extrabiblical-photo-button-template.png"),
        cacheUrl("ptp-models-photo-button-template.png")
      ]),
    ),
  );
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) =>
        Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))),
      ),
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;
  if (event.request.mode === "navigate") {
    event.respondWith(
      fetch(event.request).catch(() => caches.match(cacheUrl("./"))),
    );
    return;
  }
  event.respondWith(
    caches.match(event.request).then((cached) => cached ?? fetch(event.request)),
  );
});
