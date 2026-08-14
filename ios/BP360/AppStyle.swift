import UIKit

enum AppStyle {
    static let background = UIColor(hex: 0x05242F)
    static let card = UIColor(hex: 0x0A3645)
    static let border = UIColor(hex: 0x1A5367)
    static let subtext = UIColor.lightGray
    static let white = UIColor.white

    static func titleFont(_ size: CGFloat) -> UIFont {
        .systemFont(ofSize: size, weight: .medium)
    }

    static func monoFont(_ size: CGFloat) -> UIFont {
        .monospacedSystemFont(ofSize: size, weight: .regular)
    }

    static func makeTopBanner() -> UIImageView {
        let imageView = UIImageView(image: UIImage(named: "home_banner"))
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }

    static func makePrimaryButton() -> UIControl {
        let control = UIControl()
        control.backgroundColor = card
        control.layer.cornerRadius = 28
        control.layer.borderWidth = 2
        control.layer.borderColor = border.cgColor
        control.clipsToBounds = true
        control.translatesAutoresizingMaskIntoConstraints = false
        return control
    }
}

extension UIColor {
    convenience init(hex: UInt32) {
        let red = CGFloat((hex >> 16) & 0xFF) / 255
        let green = CGFloat((hex >> 8) & 0xFF) / 255
        let blue = CGFloat(hex & 0xFF) / 255
        self.init(red: red, green: green, blue: blue, alpha: 1)
    }
}

extension UIView {
    func pinToSuperview() {
        guard let superview = superview else { return }
        translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            topAnchor.constraint(equalTo: superview.topAnchor),
            leadingAnchor.constraint(equalTo: superview.leadingAnchor),
            trailingAnchor.constraint(equalTo: superview.trailingAnchor),
            bottomAnchor.constraint(equalTo: superview.bottomAnchor)
        ])
    }
}
