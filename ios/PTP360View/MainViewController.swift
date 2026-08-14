import UIKit

final class MainViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = AppStyle.background

        let scrollView = UIScrollView()
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(scrollView)

        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor)
        ])

        let layout = UIStackView()
        layout.axis = .vertical
        layout.alignment = .fill
        layout.translatesAutoresizingMaskIntoConstraints = false
        scrollView.addSubview(layout)

        NSLayoutConstraint.activate([
            layout.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor, constant: 18),
            layout.leadingAnchor.constraint(equalTo: scrollView.frameLayoutGuide.leadingAnchor),
            layout.trailingAnchor.constraint(equalTo: scrollView.frameLayoutGuide.trailingAnchor),
            layout.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor, constant: -30)
        ])

        let banner = AppStyle.makeTopBanner()
        layout.addArrangedSubview(banner)
        NSLayoutConstraint.activate([
            banner.heightAnchor.constraint(equalTo: banner.widthAnchor, multiplier: 0.34)
        ])
        layout.setCustomSpacing(18, after: banner)

        let content = UIStackView()
        content.axis = .vertical
        content.alignment = .center
        content.layoutMargins = UIEdgeInsets(top: 0, left: 42, bottom: 0, right: 42)
        content.isLayoutMarginsRelativeArrangement = true
        layout.addArrangedSubview(content)

        let subtitle = UILabel()
        subtitle.text = "360° Photos & Videos"
        subtitle.font = AppStyle.monoFont(20)
        subtitle.textColor = AppStyle.subtext
        subtitle.textAlignment = .center
        content.addArrangedSubview(subtitle)
        content.setCustomSpacing(24, after: subtitle)

        content.addArrangedSubview(menuImage(named: "video_gallery") { [weak self] in
            self?.navigationController?.pushViewController(MediaListViewController(kind: .videos), animated: true)
        })
        content.setCustomSpacing(12, after: content.arrangedSubviews.last!)

        content.addArrangedSubview(menuImage(named: "photo_gallery") { [weak self] in
            self?.navigationController?.pushViewController(MediaListViewController(kind: .photos), animated: true)
        })
        content.setCustomSpacing(72, after: content.arrangedSubviews.last!)

        content.addArrangedSubview(menuImage(named: "about_us_button", aspectRatio: 0.54) { [weak self] in
            self?.navigationController?.pushViewController(AboutViewController(), animated: true)
        })
    }

    private func menuImage(named imageName: String, aspectRatio: CGFloat = 0.46, action: @escaping () -> Void) -> UIControl {
        let button = AppStyle.makePrimaryButton()
        button.addAction(UIAction { _ in action() }, for: .touchUpInside)

        let imageView = UIImageView(image: UIImage(named: imageName))
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        button.addSubview(imageView)

        let targetWidth = button.widthAnchor.constraint(equalToConstant: 500)
        targetWidth.priority = UILayoutPriority(999)
        let availableWidth = button.widthAnchor.constraint(equalTo: view.widthAnchor, constant: -84)
        availableWidth.priority = UILayoutPriority(998)

        NSLayoutConstraint.activate([
            imageView.topAnchor.constraint(equalTo: button.topAnchor),
            imageView.leadingAnchor.constraint(equalTo: button.leadingAnchor),
            imageView.trailingAnchor.constraint(equalTo: button.trailingAnchor),
            imageView.bottomAnchor.constraint(equalTo: button.bottomAnchor),
            button.widthAnchor.constraint(lessThanOrEqualToConstant: 500),
            button.widthAnchor.constraint(lessThanOrEqualTo: view.widthAnchor, constant: -84),
            targetWidth,
            availableWidth,
            button.heightAnchor.constraint(equalTo: button.widthAnchor, multiplier: aspectRatio)
        ])

        return button
    }
}
