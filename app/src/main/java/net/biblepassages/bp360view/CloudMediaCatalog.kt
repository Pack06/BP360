package net.biblepassages.bp360view

data class CloudMediaItem(
    val title: String,
    val url: String,
    val category: String
)

data class CloudMediaCatalog(
    val videos: List<CloudMediaItem>,
    val pictures: List<CloudMediaItem>
)

object CloudMediaCatalogLoader {
    private val builtInVideos = listOf(
        CloudMediaItem(
            title = "Esos",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/9c31b0783aaa99f11c0fe039d389cd09/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Rome Forum",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/7c811eba2fba5b2f77bbd15598b7fcf4/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Smyrna",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/2efc242881fd7a0a085749f3f70185ab/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Ephesus-Curetes Street",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/5dfacddd82a8a552c0986a0db12fc395/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Hieropolis",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/886452245090c8d0107685d234542216/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Terrace Houses",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/33818175191ec50a8ed6c8bba3740998/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Laodicia",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/bdb602b78317f234a3d26dd66a976eeb/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Didyma",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/57a5797f53c0e7f8d724eb25b7b3b359/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Miletus",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/7ae09517f5d84458f4ecd146b6845771/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Colosseum",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/9bcd1a5d022c35bedd9c604923592060/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Ephesus-Harbor Street",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/77dd6d37236d3e03129ecb30aa887262/manifest/video.m3u8",
            category = "Walking Videos"
        ),
        CloudMediaItem(
            title = "Arch of Titus",
            url = "https://customer-jew7aqoelzighbi2.cloudflarestream.com/9563608e898fdf39533b103deb1c4907/manifest/video.m3u8",
            category = "Walking Videos"
        )
    )
    private val builtInPictures = listOf(
        CloudMediaItem(
            title = "Corinth Agora",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/79290fb4-e4d7-403e-804b-c9bedaad4800/hires",
            category = "Biblical Sites"
        ),
        CloudMediaItem(
            title = "Pergamum",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/a0d1802a-0c2e-4e17-0649-e0f2d0021e00/hires",
            category = "Biblical Sites"
        ),
        CloudMediaItem(
            title = "Cencrea",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/c41faf21-5d8f-46b0-6e68-ce253a270d00/hires",
            category = "Biblical Sites"
        ),
        CloudMediaItem(
            title = "Assos",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/185069bf-2640-47c9-c489-b2a5e4fe2800/hires",
            category = "Biblical Sites"
        ),
        CloudMediaItem(
            title = "Athens Agora",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/d0ef8727-2085-46dd-e422-6481273b7d00/hires",
            category = "Biblical Sites"
        ),
        CloudMediaItem(
            title = "Pyramids of Egypt",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/9322e8d3-a5fa-403c-bbaf-f1a436ebe300/hires",
            category = "Extrabiblical Sites"
        ),
        CloudMediaItem(
            title = "Hagia Sophia",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/00d6e9bb-c45a-44bf-91ae-5bb9b06c0d00/hires",
            category = "Extrabiblical Sites"
        ),
        CloudMediaItem(
            title = "Colosseum",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/ef7f360f-ddc6-4a69-2d72-612d5780ee00/hires",
            category = "Extrabiblical Sites"
        ),
        CloudMediaItem(
            title = "Tomb of Ramses I",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/f1462c82-b782-48d1-cb87-60349998d500/hires",
            category = "Extrabiblical Sites"
        ),
        CloudMediaItem(
            title = "Herod's Temple",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/ab0bed57-b260-4f80-c428-6dfb3fa8c600/hires",
            category = "Reconstructed Models"
        ),
        CloudMediaItem(
            title = "Tabernacle",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/8ee64c87-fb62-442c-cedf-88c9117e2c00/hires",
            category = "Reconstructed Models"
        ),
        CloudMediaItem(
            title = "Solomon's Temple",
            url = "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/9179ad9a-ab28-47f1-0422-3c4199e67800/hires",
            category = "Reconstructed Models"
        )
    )

    fun load(
        onLoaded: (CloudMediaCatalog) -> Unit
    ) {
        onLoaded(
            CloudMediaCatalog(
                videos = builtInVideos,
                pictures = builtInPictures
            )
        )
    }
}
