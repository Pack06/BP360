import CoreMotion
import SceneKit
import UIKit

class PanoramaSceneController: UIViewController {
    let sceneView = SCNView()
    let cameraNode = SCNNode()
    let sphereNode = SCNNode(geometry: SCNSphere(radius: 10))
    private let motionManager = CMMotionManager()
    private var yaw: CGFloat = 0
    private var pitch: CGFloat = 0
    private var motionReferenceAttitude: CMAttitude?
    private var useMotionControl = false
    private var sceneIsReady = false

    override var prefersStatusBarHidden: Bool {
        true
    }

    override var prefersHomeIndicatorAutoHidden: Bool {
        true
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        stopMotionUpdates()
    }

    func showControlModeChoice(onChoice: @escaping (Bool) -> Void) {
        view.backgroundColor = AppStyle.background

        let layout = UIStackView()
        layout.axis = .vertical
        layout.alignment = .fill
        layout.spacing = 12
        layout.layoutMargins = UIEdgeInsets(top: 44, left: 44, bottom: 44, right: 44)
        layout.isLayoutMarginsRelativeArrangement = true
        layout.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(layout)

        let title = UILabel()
        title.text = "Choose Control Mode"
        title.font = AppStyle.titleFont(28)
        title.textColor = AppStyle.white
        title.textAlignment = .center
        title.numberOfLines = 0
        layout.addArrangedSubview(title)
        layout.setCustomSpacing(36, after: title)

        layout.addArrangedSubview(controlModeButton(title: "Touch Control") {
            onChoice(false)
        })
        layout.addArrangedSubview(controlModeButton(title: "Motion Control") {
            onChoice(true)
        })

        NSLayoutConstraint.activate([
            layout.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            layout.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            layout.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])
    }

    func startPanoramaScene(useMotionControl: Bool) {
        self.useMotionControl = useMotionControl
        motionReferenceAttitude = nil
        view.subviews.forEach { $0.removeFromSuperview() }
        view.backgroundColor = .black
        buildScene()
        installGestures()
        sceneIsReady = true
        startMotionUpdatesIfNeeded()
    }

    func setPanoramaContents(_ contents: Any?) {
        let material = sphereNode.geometry?.firstMaterial
        material?.diffuse.contents = contents
        material?.isDoubleSided = true
        material?.lightingModel = .constant
        material?.cullMode = .front
    }

    func showMessage(_ text: String) {
        view.subviews.forEach { $0.removeFromSuperview() }
        let label = UILabel()
        label.text = text
        label.font = AppStyle.titleFont(20)
        label.textColor = AppStyle.white
        label.textAlignment = .center
        label.numberOfLines = 0
        label.backgroundColor = AppStyle.background
        label.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(label)
        label.pinToSuperview()
    }

    func makeLoadingView() -> UILabel {
        let label = UILabel()
        label.text = "Loading..."
        label.font = AppStyle.titleFont(24)
        label.textColor = AppStyle.white
        label.textAlignment = .center
        label.backgroundColor = AppStyle.background
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }

    private func controlModeButton(title: String, action: @escaping () -> Void) -> UIControl {
        let button = UIControl()
        button.backgroundColor = AppStyle.card
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addAction(UIAction { _ in action() }, for: .touchUpInside)

        let label = UILabel()
        label.text = title
        label.font = AppStyle.titleFont(23)
        label.textColor = AppStyle.white
        label.textAlignment = .center
        label.translatesAutoresizingMaskIntoConstraints = false
        button.addSubview(label)

        NSLayoutConstraint.activate([
            label.topAnchor.constraint(equalTo: button.topAnchor, constant: 28),
            label.leadingAnchor.constraint(equalTo: button.leadingAnchor, constant: 16),
            label.trailingAnchor.constraint(equalTo: button.trailingAnchor, constant: -16),
            label.bottomAnchor.constraint(equalTo: button.bottomAnchor, constant: -28)
        ])

        return button
    }

    private func buildScene() {
        sceneView.backgroundColor = .black
        sceneView.translatesAutoresizingMaskIntoConstraints = false
        sceneView.allowsCameraControl = false
        view.addSubview(sceneView)
        sceneView.pinToSuperview()

        let scene = SCNScene()
        sceneView.scene = scene

        let sphere = sphereNode.geometry as? SCNSphere
        sphere?.segmentCount = 96
        sphereNode.scale = SCNVector3(-1, 1, 1)
        scene.rootNode.addChildNode(sphereNode)

        let camera = SCNCamera()
        camera.fieldOfView = 90
        camera.zNear = 0.1
        camera.zFar = 100
        cameraNode.camera = camera
        cameraNode.position = SCNVector3Zero
        scene.rootNode.addChildNode(cameraNode)
        sceneView.pointOfView = cameraNode
    }

    private func installGestures() {
        let pan = UIPanGestureRecognizer(target: self, action: #selector(handlePan(_:)))
        let pinch = UIPinchGestureRecognizer(target: self, action: #selector(handlePinch(_:)))
        view.addGestureRecognizer(pan)
        view.addGestureRecognizer(pinch)
    }

    @objc private func handlePan(_ recognizer: UIPanGestureRecognizer) {
        guard !useMotionControl else { return }
        let translation = recognizer.translation(in: view)
        yaw += translation.x * 0.2
        pitch += translation.y * 0.2
        pitch = min(89, max(-89, pitch))
        recognizer.setTranslation(.zero, in: view)
        updateCameraRotation()
    }

    @objc private func handlePinch(_ recognizer: UIPinchGestureRecognizer) {
        guard let camera = cameraNode.camera else { return }
        let delta = (1 - recognizer.scale) * 18
        camera.fieldOfView = min(120, max(30, camera.fieldOfView + delta))
        recognizer.scale = 1
    }

    private func updateCameraRotation() {
        cameraNode.eulerAngles = SCNVector3(
            Float(pitch * .pi / 180),
            Float(yaw * .pi / 180),
            0
        )
    }

    private func startMotionUpdatesIfNeeded() {
        guard useMotionControl, sceneIsReady, motionManager.isDeviceMotionAvailable else {
            return
        }

        motionManager.deviceMotionUpdateInterval = 1.0 / 60.0
        motionManager.startDeviceMotionUpdates(using: .xArbitraryCorrectedZVertical, to: .main) { [weak self] motion, _ in
            guard let self = self, let attitude = motion?.attitude else { return }

            if self.motionReferenceAttitude == nil {
                self.motionReferenceAttitude = attitude.copy() as? CMAttitude
            }

            if let reference = self.motionReferenceAttitude {
                attitude.multiply(byInverseOf: reference)
            }

            self.cameraNode.eulerAngles = SCNVector3(
                Float(-attitude.pitch),
                Float(-attitude.yaw),
                0
            )
        }
    }

    private func stopMotionUpdates() {
        motionManager.stopDeviceMotionUpdates()
    }
}
