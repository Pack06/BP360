import UIKit

final class FooterButton: UIControl {
    private let label = UILabel()

    var title: String {
        get { label.text ?? "" }
        set { label.text = newValue }
    }

    init(title: String) {
        super.init(frame: .zero)
        self.title = title
        backgroundColor = AppStyle.card
        translatesAutoresizingMaskIntoConstraints = false

        label.font = AppStyle.titleFont(22)
        label.textColor = AppStyle.white
        label.textAlignment = .center
        label.translatesAutoresizingMaskIntoConstraints = false
        addSubview(label)

        NSLayoutConstraint.activate([
            heightAnchor.constraint(greaterThanOrEqualToConstant: 70),
            label.topAnchor.constraint(equalTo: topAnchor, constant: 24),
            label.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 16),
            label.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -16),
            label.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -24)
        ])
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
