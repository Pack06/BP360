import UIKit

final class PanoramaPhotoViewController: PanoramaSceneController {
    private let url: URL
    private var loadingView: UILabel?

    init(url: URL) {
        self.url = url
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        showControlModeChoice { [weak self] useMotionControl in
            self?.startPanorama(useMotionControl: useMotionControl)
        }
    }

    private func startPanorama(useMotionControl: Bool) {
        startPanoramaScene(useMotionControl: useMotionControl)
        addLoadingOverlay()
        loadImage()
    }

    private func loadImage() {
        URLSession.shared.dataTask(with: url) { [weak self] data, _, error in
            guard let self = self else { return }

            if let data = data, let image = UIImage(data: data) {
                DispatchQueue.main.async {
                    self.setPanoramaContents(image)
                    self.showLoading(false)
                }
            } else {
                DispatchQueue.main.async {
                    self.showMessage(error?.localizedDescription ?? "Picture could not load.")
                }
            }
        }.resume()
    }

    private func addLoadingOverlay() {
        let loadingView = makeLoadingView()
        self.loadingView = loadingView
        view.addSubview(loadingView)
        loadingView.pinToSuperview()
        showLoading(true)
    }

    private func showLoading(_ isLoading: Bool) {
        loadingView?.isHidden = !isLoading
    }
}
