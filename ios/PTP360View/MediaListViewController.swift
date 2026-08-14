import UIKit

final class MediaListViewController: UIViewController {
    enum Kind: Equatable {
        case videos
        case photos

        var title: String {
            switch self {
            case .videos:
                return "Videos"
            case .photos:
                return "Photos"
            }
        }

        var loadingMessage: String {
            switch self {
            case .videos:
                return "Loading videos..."
            case .photos:
                return "Loading pictures..."
            }
        }

        var emptyMessage: String {
            switch self {
            case .videos:
                return "No Cloudflare videos are configured yet."
            case .photos:
                return "No Cloudflare pictures are configured yet."
            }
        }

        var emptyFolderMessage: String {
            switch self {
            case .videos:
                return "No videos are in this folder yet."
            case .photos:
                return "No photos are in this folder yet."
            }
        }

        var backgroundImageName: String {
            switch self {
            case .videos:
                return "videos_background"
            case .photos:
                return "photos_background"
            }
        }

        var categories: [String] {
            switch self {
            case .videos:
                return ["Walking Videos", "Conv Videos", "PTP Videos"]
            case .photos:
                return ["Biblical Sites", "Extrabiblical Sites", "Reconstructed Models"]
            }
        }

        func photoTemplateName(for category: String) -> String {
            if category.compare("Biblical Sites", options: .caseInsensitive) == .orderedSame {
                return "biblical_photo_button_template"
            }
            if category.compare("Extrabiblical Sites", options: .caseInsensitive) == .orderedSame {
                return "extrabiblical_photo_button_template"
            }
            if category.compare("Reconstructed Models", options: .caseInsensitive) == .orderedSame {
                return "models_photo_button_template"
            }
            return "biblical_photo_button_template"
        }
    }

    private let kind: Kind
    private let header = UILabel()
    private let listContainer = UIStackView()
    private let footerButton = FooterButton(title: "< Back")
    private var catalog: CloudMediaCatalog?
    private var selectedCategory: String?

    init(kind: Kind) {
        self.kind = kind
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        buildLayout()
        showMessage(kind.loadingMessage)

        CloudMediaCatalogLoader.load { [weak self] catalog in
            self?.catalog = catalog
            self?.showCategories()
        }
    }

    private func buildLayout() {
        view.backgroundColor = AppStyle.background

        let background = UIImageView(image: UIImage(named: kind.backgroundImageName))
        background.contentMode = .scaleAspectFill
        background.clipsToBounds = true
        view.addSubview(background)
        background.pinToSuperview()

        let overlay = UIView()
        overlay.backgroundColor = AppStyle.background.withAlphaComponent(0.55)
        view.addSubview(overlay)
        overlay.pinToSuperview()

        let scrollView = UIScrollView()
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(scrollView)

        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor)
        ])

        let root = UIStackView()
        root.axis = .vertical
        root.translatesAutoresizingMaskIntoConstraints = false
        scrollView.addSubview(root)

        NSLayoutConstraint.activate([
            root.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor, constant: 18),
            root.leadingAnchor.constraint(equalTo: scrollView.frameLayoutGuide.leadingAnchor),
            root.trailingAnchor.constraint(equalTo: scrollView.frameLayoutGuide.trailingAnchor),
            root.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor, constant: -40),
            root.heightAnchor.constraint(greaterThanOrEqualTo: scrollView.frameLayoutGuide.heightAnchor, constant: -58)
        ])

        let banner = AppStyle.makeTopBanner()
        root.addArrangedSubview(banner)
        NSLayoutConstraint.activate([
            banner.heightAnchor.constraint(equalTo: banner.widthAnchor, multiplier: 0.34)
        ])
        root.setCustomSpacing(18, after: banner)

        let content = UIStackView()
        content.axis = .vertical
        content.layoutMargins = UIEdgeInsets(top: 0, left: 40, bottom: 0, right: 40)
        content.isLayoutMarginsRelativeArrangement = true
        root.addArrangedSubview(content)

        header.text = kind.title
        header.font = AppStyle.titleFont(32)
        header.textColor = AppStyle.white
        header.numberOfLines = 0
        content.addArrangedSubview(header)
        content.setCustomSpacing(30, after: header)

        listContainer.axis = .vertical
        content.addArrangedSubview(listContainer)

        let spacer = UIView()
        root.addArrangedSubview(spacer)
        spacer.setContentHuggingPriority(.defaultLow, for: .vertical)

        footerButton.addTarget(self, action: #selector(footerTapped), for: .touchUpInside)
        content.addArrangedSubview(footerButton)
    }

    private func showCategories() {
        selectedCategory = nil
        header.text = kind.title
        footerButton.title = "< Back"
        removeRows()

        let configuredItems = kind == .videos ? catalog?.videos : catalog?.pictures
        guard let items = configuredItems, !items.isEmpty else {
            showMessage(kind.emptyMessage)
            return
        }

        for category in kind.categories {
            listContainer.addArrangedSubview(categoryButton(for: category) { [weak self] in
                self?.showItems(in: category)
            })
        }
    }

    private func showItems(in category: String) {
        selectedCategory = category
        header.text = category
        footerButton.title = kind == .videos ? "< Videos" : "< Photos"
        removeRows()

        let source = kind == .videos ? catalog?.videos : catalog?.pictures
        let items = source?.filter { $0.category.compare(category, options: .caseInsensitive) == .orderedSame } ?? []

        guard !items.isEmpty else {
            showMessage(kind.emptyFolderMessage)
            return
        }

        switch kind {
        case .videos where category.compare("Walking Videos", options: .caseInsensitive) == .orderedSame:
            addImageGrid(
                items,
                imageName: "walking_video_button_template",
                overlayTitle: walkingButtonTitle,
                overlayTextSize: walkingButtonTextSize
            )
        case .videos where category.compare("PTP Videos", options: .caseInsensitive) == .orderedSame:
            addImageGrid(items, imageName: "ptp_video_button_template")
        case .photos:
            addImageGrid(
                items,
                imageName: kind.photoTemplateName(for: category),
                overlayTitle: photoButtonTitle,
                overlayTextSize: photoButtonTextSize
            )
        default:
            for item in items {
                listContainer.addArrangedSubview(mediaRow(title: item.title) { [weak self] in
                    self?.open(item)
                })
            }
        }
    }

    private func categoryButton(for category: String, action: @escaping () -> Void) -> UIView {
        switch kind {
        case .videos where category.compare("Walking Videos", options: .caseInsensitive) == .orderedSame:
            return centeredImageButton(
                imageName: "walking_video_button_template",
                title: category,
                action: action,
                overlayTitle: "WALKING\nVIDEOS",
                overlayTextSize: 13.5,
                overlayHeightRatio: 0.25,
                overlayBottomRatio: 0.146
            )
        case .videos where category.compare("Conv Videos", options: .caseInsensitive) == .orderedSame:
            return centeredImageButton(imageName: "conversation_videos_category_button", title: category, action: action)
        case .videos where category.compare("PTP Videos", options: .caseInsensitive) == .orderedSame:
            return centeredImageButton(imageName: "ptp_videos_category_button", title: category, action: action)
        case .photos:
            return centeredImageButton(
                imageName: kind.photoTemplateName(for: category),
                title: category,
                action: action,
                overlayTitle: photoButtonTitle(category),
                overlayTextSize: photoButtonTextSize(category),
                overlayHeightRatio: 0.274,
                overlayBottomRatio: 0.116
            )
        default:
            return mediaRow(title: category, action: action)
        }
    }

    private func centeredImageButton(
        imageName: String,
        title: String,
        action: @escaping () -> Void,
        overlayTitle: String? = nil,
        overlayTextSize: CGFloat = 12,
        overlayHeightRatio: CGFloat = 0.26,
        overlayBottomRatio: CGFloat = 0.1
    ) -> UIView {
        let button = imageButton(
            imageName: imageName,
            title: title,
            action: action,
            overlayTitle: overlayTitle,
            overlayTextSize: overlayTextSize,
            overlayHeightRatio: overlayHeightRatio,
            overlayBottomRatio: overlayBottomRatio
        )

        let wrapper = UIStackView(arrangedSubviews: [button])
        wrapper.axis = .vertical
        wrapper.alignment = .center
        wrapper.layoutMargins = UIEdgeInsets(top: 8, left: 0, bottom: 18, right: 0)
        wrapper.isLayoutMarginsRelativeArrangement = true

        let width = button.widthAnchor.constraint(equalToConstant: 520)
        width.priority = UILayoutPriority(999)
        let availableWidth = button.widthAnchor.constraint(equalTo: view.widthAnchor, constant: -80)
        availableWidth.priority = UILayoutPriority(998)
        NSLayoutConstraint.activate([
            button.widthAnchor.constraint(lessThanOrEqualToConstant: 520),
            button.widthAnchor.constraint(lessThanOrEqualTo: view.widthAnchor, constant: -80),
            width,
            availableWidth,
            button.heightAnchor.constraint(equalTo: button.widthAnchor)
        ])

        return wrapper
    }

    private func addImageGrid(
        _ items: [CloudMediaItem],
        imageName: String,
        overlayTitle: ((String) -> String)? = nil,
        overlayTextSize: ((String) -> CGFloat)? = nil
    ) {
        for pairStart in stride(from: 0, to: items.count, by: 2) {
            let row = UIStackView()
            row.axis = .horizontal
            row.distribution = .fillEqually
            row.alignment = .fill
            row.spacing = 12

            let rowItems = Array(items[pairStart..<min(pairStart + 2, items.count)])
            for item in rowItems {
                let button = imageButton(
                    imageName: imageName,
                    title: item.title,
                    action: { [weak self] in self?.open(item) },
                    overlayTitle: overlayTitle?(item.title),
                    overlayTextSize: overlayTextSize?(item.title) ?? 12,
                    overlayHeightRatio: kind == .photos ? 0.274 : 0.26,
                    overlayBottomRatio: kind == .photos ? 0.116 : 0.098
                )
                row.addArrangedSubview(button)
                button.heightAnchor.constraint(equalTo: button.widthAnchor).isActive = true
            }

            if rowItems.count == 1 {
                row.addArrangedSubview(UIView())
            }

            let wrapper = UIStackView(arrangedSubviews: [row])
            wrapper.axis = .vertical
            wrapper.layoutMargins = UIEdgeInsets(top: 6, left: 0, bottom: 14, right: 0)
            wrapper.isLayoutMarginsRelativeArrangement = true
            listContainer.addArrangedSubview(wrapper)
        }
    }

    private func imageButton(
        imageName: String,
        title: String,
        action: @escaping () -> Void,
        overlayTitle: String? = nil,
        overlayTextSize: CGFloat = 12,
        overlayHeightRatio: CGFloat = 0.26,
        overlayBottomRatio: CGFloat = 0.1
    ) -> UIControl {
        let button = UIControl()
        button.accessibilityLabel = title
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addAction(UIAction { _ in action() }, for: .touchUpInside)

        let imageView = UIImageView(image: UIImage(named: imageName))
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        button.addSubview(imageView)
        imageView.pinToSuperview()

        if let overlayTitle {
            let label = UILabel()
            label.text = overlayTitle
            label.font = .systemFont(ofSize: overlayTextSize, weight: .bold)
            label.textColor = AppStyle.white
            label.textAlignment = .center
            label.numberOfLines = 2
            label.adjustsFontSizeToFitWidth = true
            label.minimumScaleFactor = 0.72
            label.translatesAutoresizingMaskIntoConstraints = false
            button.addSubview(label)

            NSLayoutConstraint.activate([
                label.leadingAnchor.constraint(equalTo: button.leadingAnchor, constant: 22),
                label.trailingAnchor.constraint(equalTo: button.trailingAnchor, constant: -22),
                label.heightAnchor.constraint(equalTo: button.heightAnchor, multiplier: overlayHeightRatio),
                label.bottomAnchor.constraint(equalTo: button.bottomAnchor, constant: -overlayBottomRatio * 520)
            ])
        }

        return button
    }

    private func open(_ item: CloudMediaItem) {
        switch kind {
        case .videos:
            navigationController?.pushViewController(PanoramaVideoViewController(url: item.url), animated: true)
        case .photos:
            navigationController?.pushViewController(PanoramaPhotoViewController(url: item.url), animated: true)
        }
    }

    private func mediaRow(title: String, action: @escaping () -> Void) -> UIView {
        let row = UIControl()
        row.backgroundColor = AppStyle.card
        row.translatesAutoresizingMaskIntoConstraints = false
        row.addAction(UIAction { _ in action() }, for: .touchUpInside)

        let label = UILabel()
        label.text = title
        label.font = AppStyle.titleFont(21)
        label.textColor = AppStyle.white
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false
        row.addSubview(label)

        NSLayoutConstraint.activate([
            label.topAnchor.constraint(equalTo: row.topAnchor, constant: 24),
            label.leadingAnchor.constraint(equalTo: row.leadingAnchor, constant: 28),
            label.trailingAnchor.constraint(equalTo: row.trailingAnchor, constant: -28),
            label.bottomAnchor.constraint(equalTo: row.bottomAnchor, constant: -24)
        ])

        let wrapper = UIStackView(arrangedSubviews: [row])
        wrapper.axis = .vertical
        wrapper.layoutMargins = UIEdgeInsets(top: 10, left: 0, bottom: 10, right: 0)
        wrapper.isLayoutMarginsRelativeArrangement = true
        return wrapper
    }

    private func showMessage(_ message: String) {
        removeRows()
        let label = UILabel()
        label.text = message
        label.font = AppStyle.monoFont(18)
        label.textColor = AppStyle.subtext
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false
        let wrapper = UIStackView(arrangedSubviews: [label])
        wrapper.layoutMargins = UIEdgeInsets(top: 8, left: 8, bottom: 24, right: 8)
        wrapper.isLayoutMarginsRelativeArrangement = true
        listContainer.addArrangedSubview(wrapper)
    }

    private func removeRows() {
        for view in listContainer.arrangedSubviews {
            listContainer.removeArrangedSubview(view)
            view.removeFromSuperview()
        }
    }

    @objc private func footerTapped() {
        if selectedCategory != nil {
            showCategories()
        } else {
            navigationController?.popViewController(animated: true)
        }
    }

    private func walkingButtonTitle(_ title: String) -> String {
        let upperTitle = title.uppercased()
        if upperTitle.contains("-") {
            return upperTitle.replacingOccurrences(of: "-", with: "\n")
        }

        let words = upperTitle.split(separator: " ").map(String.init)
        if words.count == 2 {
            return words.joined(separator: "\n")
        }
        if words.count > 2 {
            return "\(words.dropLast().joined(separator: " "))\n\(words.last ?? "")"
        }
        return upperTitle
    }

    private func walkingButtonTextSize(_ title: String) -> CGFloat {
        let longestLine = walkingButtonTitle(title).split(separator: "\n").map { $0.count }.max() ?? title.count
        if longestLine > 16 {
            return 10.5
        }
        if longestLine > 12 {
            return 11.5
        }
        return 12.5
    }

    private func photoButtonTitle(_ title: String) -> String {
        switch title {
        case "Pyramids of Egypt":
            return "PYRAMIDS\nOF EGYPT"
        case "Tomb of Ramses I":
            return "TOMB OF\nRAMSES I"
        default:
            let words = title.uppercased().split(separator: " ").map(String.init)
            if words.count == 2 {
                return words.joined(separator: "\n")
            }
            if words.count > 2 {
                return "\(words.dropLast().joined(separator: " "))\n\(words.last ?? "")"
            }
            return title.uppercased()
        }
    }

    private func photoButtonTextSize(_ title: String) -> CGFloat {
        let longestLine = photoButtonTitle(title).split(separator: "\n").map { $0.count }.max() ?? title.count
        if longestLine > 16 {
            return 10.5
        }
        if longestLine > 12 {
            return 11.5
        }
        return 12.5
    }
}
