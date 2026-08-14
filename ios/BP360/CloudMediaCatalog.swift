import Foundation

struct CloudMediaItem {
    let title: String
    let url: URL
    let category: String
}

struct CloudMediaCatalog {
    let videos: [CloudMediaItem]
    let pictures: [CloudMediaItem]
}

enum CloudMediaCatalogLoader {
    static func load(completion: @escaping (CloudMediaCatalog) -> Void) {
        completion(
            CloudMediaCatalog(
                videos: builtInVideos,
                pictures: builtInPictures
            )
        )
    }

    private static func item(_ title: String, _ url: String, _ category: String) -> CloudMediaItem {
        CloudMediaItem(title: title, url: URL(string: url)!, category: category)
    }

    private static let builtInVideos = [
        item("Esos", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/9c31b0783aaa99f11c0fe039d389cd09/manifest/video.m3u8", "Walking Videos"),
        item("Rome Forum", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/7c811eba2fba5b2f77bbd15598b7fcf4/manifest/video.m3u8", "Walking Videos"),
        item("Smyrna", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/2efc242881fd7a0a085749f3f70185ab/manifest/video.m3u8", "Walking Videos"),
        item("Ephesus-Curetes Street", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/5dfacddd82a8a552c0986a0db12fc395/manifest/video.m3u8", "Walking Videos"),
        item("Hieropolis", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/886452245090c8d0107685d234542216/manifest/video.m3u8", "Walking Videos"),
        item("Terrace Houses", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/33818175191ec50a8ed6c8bba3740998/manifest/video.m3u8", "Walking Videos"),
        item("Laodicia", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/bdb602b78317f234a3d26dd66a976eeb/manifest/video.m3u8", "Walking Videos"),
        item("Didyma", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/57a5797f53c0e7f8d724eb25b7b3b359/manifest/video.m3u8", "Walking Videos"),
        item("Miletus", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/7ae09517f5d84458f4ecd146b6845771/manifest/video.m3u8", "Walking Videos"),
        item("Colosseum", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/9bcd1a5d022c35bedd9c604923592060/manifest/video.m3u8", "Walking Videos"),
        item("Ephesus-Harbor Street", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/77dd6d37236d3e03129ecb30aa887262/manifest/video.m3u8", "Walking Videos"),
        item("Arch of Titus", "https://customer-jew7aqoelzighbi2.cloudflarestream.com/9563608e898fdf39533b103deb1c4907/manifest/video.m3u8", "Walking Videos")
    ]

    private static let builtInPictures = [
        item("Corinth Agora", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/79290fb4-e4d7-403e-804b-c9bedaad4800/hires", "Biblical Sites"),
        item("Pergamum", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/a0d1802a-0c2e-4e17-0649-e0f2d0021e00/hires", "Biblical Sites"),
        item("Cencrea", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/c41faf21-5d8f-46b0-6e68-ce253a270d00/hires", "Biblical Sites"),
        item("Assos", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/185069bf-2640-47c9-c489-b2a5e4fe2800/hires", "Biblical Sites"),
        item("Athens Agora", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/d0ef8727-2085-46dd-e422-6481273b7d00/hires", "Biblical Sites"),
        item("Pyramids of Egypt", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/9322e8d3-a5fa-403c-bbaf-f1a436ebe300/hires", "Extrabiblical Sites"),
        item("Hagia Sophia", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/00d6e9bb-c45a-44bf-91ae-5bb9b06c0d00/hires", "Extrabiblical Sites"),
        item("Colosseum", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/ef7f360f-ddc6-4a69-2d72-612d5780ee00/hires", "Extrabiblical Sites"),
        item("Tomb of Ramses I", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/f1462c82-b782-48d1-cb87-60349998d500/hires", "Extrabiblical Sites"),
        item("Herod's Temple", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/ab0bed57-b260-4f80-c428-6dfb3fa8c600/hires", "Reconstructed Models"),
        item("Tabernacle", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/8ee64c87-fb62-442c-cedf-88c9117e2c00/hires", "Reconstructed Models"),
        item("Solomon's Temple", "https://imagedelivery.net/Jan_LwJ-cEXTaEiZHXSNXQ/9179ad9a-ab28-47f1-0422-3c4199e67800/hires", "Reconstructed Models")
    ]
}
