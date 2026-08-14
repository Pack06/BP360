import UIKit

final class AboutViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()

        let background = UIStackView()
        background.axis = .vertical
        background.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(background)
        background.pinToSuperview()

        for _ in 0..<2 {
            let imageView = UIImageView(image: UIImage(named: "about_background"))
            imageView.contentMode = .scaleAspectFill
            imageView.clipsToBounds = true
            background.addArrangedSubview(imageView)
            imageView.heightAnchor.constraint(equalTo: background.heightAnchor, multiplier: 0.5).isActive = true
        }

        let overlay = UIView()
        overlay.backgroundColor = AppStyle.background.withAlphaComponent(0.62)
        view.addSubview(overlay)
        overlay.pinToSuperview()

        let root = UIStackView()
        root.axis = .vertical
        root.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(root)

        NSLayoutConstraint.activate([
            root.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 18),
            root.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            root.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            root.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -60)
        ])

        let banner = AppStyle.makeTopBanner()
        root.addArrangedSubview(banner)
        NSLayoutConstraint.activate([
            banner.heightAnchor.constraint(equalTo: banner.widthAnchor, multiplier: 0.34)
        ])

        let scrollView = UIScrollView()
        root.addArrangedSubview(scrollView)

        let content = UIStackView()
        content.axis = .vertical
        content.alignment = .center
        content.layoutMargins = UIEdgeInsets(top: 0, left: 50, bottom: 0, right: 50)
        content.isLayoutMarginsRelativeArrangement = true
        content.translatesAutoresizingMaskIntoConstraints = false
        scrollView.addSubview(content)

        NSLayoutConstraint.activate([
            content.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor),
            content.leadingAnchor.constraint(equalTo: scrollView.frameLayoutGuide.leadingAnchor),
            content.trailingAnchor.constraint(equalTo: scrollView.frameLayoutGuide.trailingAnchor),
            content.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor),
            content.heightAnchor.constraint(greaterThanOrEqualTo: scrollView.frameLayoutGuide.heightAnchor)
        ])

        let title = UILabel()
        title.text = "BP360View"
        title.font = AppStyle.titleFont(36)
        title.textColor = AppStyle.white
        title.textAlignment = .center
        content.addArrangedSubview(title)

        let subtitle = UILabel()
        subtitle.text = "360° Media Viewer"
        subtitle.font = AppStyle.monoFont(20)
        subtitle.textColor = AppStyle.subtext
        subtitle.textAlignment = .center
        content.addArrangedSubview(subtitle)
        content.setCustomSpacing(40, after: subtitle)

        let body = UILabel()
        body.text = """
        One of the aims of Bible Passages is to produce high-quality, immersive 360° photographs and videos designed to transport viewers into the heart of the biblical narrative, bringing the stories of Scripture-and the actual historical locations where those events unfolded-vividly to life.

        Our main mission is the same as our Lord's: making disciples who grow more like Christ and glorify God. We do this by teaching the Bible, its story, and the places where it unfolded, serving as a resource for every Christian who wants to go deeper in their understanding of Scripture.

        Bible Passages is a 501(c)(3) nonprofit ministry made possible through the generous support of our donors and ministry partners.

        For more information, visit biblepassages.net.
        """
        body.font = AppStyle.monoFont(18)
        body.textColor = AppStyle.white
        body.textAlignment = .center
        body.numberOfLines = 0
        content.addArrangedSubview(body)

        let buttonContainer = UIStackView()
        buttonContainer.layoutMargins = UIEdgeInsets(top: 0, left: 50, bottom: 0, right: 50)
        buttonContainer.isLayoutMarginsRelativeArrangement = true
        root.addArrangedSubview(buttonContainer)

        let back = FooterButton(title: "← Back")
        back.addAction(UIAction { [weak self] _ in
            self?.navigationController?.popViewController(animated: true)
        }, for: .touchUpInside)
        buttonContainer.addArrangedSubview(back)
    }
}
