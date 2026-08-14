import AVFoundation
import SceneKit
import UIKit

final class PanoramaVideoViewController: PanoramaSceneController {
    private let originalURL: URL
    private var player: AVPlayer?
    private var statusObservation: NSKeyValueObservation?
    private var timeObserver: Any?
    private var controls = UIView()
    private var playPauseButton = UIButton(type: .system)
    private var progressSlider = UISlider()
    private var loadingView: UILabel?
    private var isSeeking = false

    init(url: URL) {
        self.originalURL = url
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        showControlModeChoice { [weak self] useMotionControl in
            self?.startPlayback(useMotionControl: useMotionControl)
        }
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        player?.pause()
    }

    deinit {
        if let timeObserver = timeObserver {
            player?.removeTimeObserver(timeObserver)
        }
    }

    private func startPlayback(useMotionControl: Bool) {
        startPanoramaScene(useMotionControl: useMotionControl)
        addLoadingOverlay()
        configurePlayer()
        buildControls()
    }

    private func configurePlayer() {
        let url = originalURL.withClientBandwidthHint("8")
        let playerItem = AVPlayerItem(url: url)
        let player = AVPlayer(playerItem: playerItem)
        self.player = player
        setPanoramaContents(player)

        statusObservation = playerItem.observe(\.status, options: [.new]) { [weak self] item, _ in
            DispatchQueue.main.async {
                switch item.status {
                case .readyToPlay:
                    self?.showLoading(false)
                    self?.player?.play()
                    self?.updatePlayPauseTitle()
                case .failed:
                    self?.showMessage("Video could not play in 3D mode.")
                case .unknown:
                    break
                @unknown default:
                    break
                }
            }
        }

        timeObserver = player.addPeriodicTimeObserver(
            forInterval: CMTime(seconds: 0.25, preferredTimescale: 600),
            queue: .main
        ) { [weak self] time in
            self?.updateProgress(currentTime: time)
        }
    }

    private func buildControls() {
        controls.backgroundColor = UIColor.black.withAlphaComponent(0.42)
        controls.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(controls)

        playPauseButton.tintColor = AppStyle.white
        playPauseButton.titleLabel?.font = AppStyle.titleFont(18)
        playPauseButton.setTitle("Pause", for: .normal)
        playPauseButton.translatesAutoresizingMaskIntoConstraints = false
        playPauseButton.addAction(UIAction { [weak self] _ in
            self?.togglePlayback()
        }, for: .touchUpInside)

        progressSlider.minimumValue = 0
        progressSlider.maximumValue = 1
        progressSlider.minimumTrackTintColor = AppStyle.white
        progressSlider.maximumTrackTintColor = AppStyle.subtext
        progressSlider.translatesAutoresizingMaskIntoConstraints = false
        progressSlider.addTarget(self, action: #selector(beginSeeking), for: .touchDown)
        progressSlider.addTarget(self, action: #selector(seekChanged), for: .valueChanged)
        progressSlider.addTarget(self, action: #selector(endSeeking), for: [.touchUpInside, .touchUpOutside, .touchCancel])

        controls.addSubview(playPauseButton)
        controls.addSubview(progressSlider)

        if let loadingView {
            view.bringSubviewToFront(loadingView)
        }

        NSLayoutConstraint.activate([
            controls.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            controls.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            controls.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
            controls.heightAnchor.constraint(equalToConstant: 66),
            playPauseButton.leadingAnchor.constraint(equalTo: controls.leadingAnchor, constant: 18),
            playPauseButton.centerYAnchor.constraint(equalTo: controls.centerYAnchor),
            playPauseButton.widthAnchor.constraint(equalToConstant: 74),
            progressSlider.leadingAnchor.constraint(equalTo: playPauseButton.trailingAnchor, constant: 14),
            progressSlider.trailingAnchor.constraint(equalTo: controls.trailingAnchor, constant: -18),
            progressSlider.centerYAnchor.constraint(equalTo: controls.centerYAnchor)
        ])
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

    private func togglePlayback() {
        if player?.timeControlStatus == .playing {
            player?.pause()
        } else {
            player?.play()
        }
        updatePlayPauseTitle()
    }

    private func updatePlayPauseTitle() {
        let title = player?.timeControlStatus == .playing ? "Pause" : "Play"
        playPauseButton.setTitle(title, for: .normal)
    }

    private func updateProgress(currentTime: CMTime) {
        guard !isSeeking,
              let duration = player?.currentItem?.duration.seconds,
              duration.isFinite,
              duration > 0 else {
            return
        }
        progressSlider.value = Float(currentTime.seconds / duration)
        updatePlayPauseTitle()
    }

    @objc private func beginSeeking() {
        isSeeking = true
    }

    @objc private func seekChanged() {
        guard let duration = player?.currentItem?.duration.seconds,
              duration.isFinite,
              duration > 0 else {
            return
        }
        let seconds = duration * Double(progressSlider.value)
        player?.seek(to: CMTime(seconds: seconds, preferredTimescale: 600), toleranceBefore: .zero, toleranceAfter: .zero)
    }

    @objc private func endSeeking() {
        seekChanged()
        isSeeking = false
    }
}

private extension URL {
    func withClientBandwidthHint(_ hint: String) -> URL {
        guard var components = URLComponents(url: self, resolvingAgainstBaseURL: false) else {
            return self
        }
        var queryItems = components.queryItems ?? []
        queryItems.removeAll { $0.name == "clientBandwidthHint" }
        queryItems.append(URLQueryItem(name: "clientBandwidthHint", value: hint))
        components.queryItems = queryItems
        return components.url ?? self
    }
}
