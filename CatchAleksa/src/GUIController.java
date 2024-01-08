import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

/**
 * The GUIController class manages the graphical user interface components for
 * the CatchAleksa game. It handles the display of backgrounds, player images,
 * fences, and various informative panels. Additionally, it provides methods for
 * updating and interacting with the user interface during gameplay.
 *
 * @author Florin
 */
public class GUIController {

	// Constants for screen dimensions
	private static final double W = 1920;
	private static final double H = 1080;

	// Lists of images for different directions
	static List<Image> imagesBack = new ArrayList<>();
	static List<Image> imagesFront = new ArrayList<>();
	static List<Image> imagesLeft = new ArrayList<>();
	static List<Image> imagesRight = new ArrayList<>();

	// Lists of images for bushes and fences
	static List<Image> imagesBushes = new ArrayList<>();
	static List<Image> imagesFences = new ArrayList<>();

	// Paths to background images
	String gamescreen = "Assets/BackGrounds/GreenBackground.png";
	ImageView fenceSide = new ImageView(new Image(("Assets/Fences/FenceSide.png")));

	private FlorinController controller;
	public Button restartButton;
	private Label labl;
	private ImageView score;

	/**
	 * Constructs a new GUIController with a reference to the FlorinController.
	 *
	 * @param controller The FlorinController instance to link with this
	 *                   GUIController.
	 */
	public GUIController(FlorinController controller) {
		this.controller = controller;
	}
	
	public ImageView getStartImageView() {
		return new ImageView(new Image("Assets/BackGrounds/Startscreen.png"));
	}

	public Image getGameBackground() {
//        java.io.InputStream inputStream = ("/Assets/BackGrounds/GreenBackground.png");
		return new Image("/Assets/BackGrounds/GreenBackground.png");
	}

	public String getplayerImage() {
		String playerImage = "Assets/WalkingAnimation/Front.png";
		return playerImage;
	}

	public ImageView getFenceImage() {
		return fenceSide;
	}

	/**
	 * Adds informative panels, including a score label, restart button, and heart
	 * images, to the scene. It also sets up event handling for the restart button.
	 *
	 * @param scene                        The JavaFX scene to which informative
	 *                                     panels will be added.
	 * @param packagesFound                The number of packages found.
	 * @param restartButtonPressedCallback The callback to be executed when the
	 *                                     restart button is pressed.
	 * @return The updated JavaFX scene.
	 */
	public Scene addInfoPannels(Scene scene, Integer packagesFound, Runnable restartButtonPressedCallback) {
		Platform.runLater(() -> {
			double x = 30;
			double y = 30;
			setScore(scene, x, y);

			labl = new Label();
			Font customFont = new Font("Monocraft", 30);
			labl.setText(controller.getPackagesFoundProperty().getValue().toString());
			labl.setFont(customFont);
			labl.relocate(175, 58);
			controller.setLabl(labl);
			((Group) scene.getRoot()).getChildren().add(labl);

			Button restart = new Button();
			ImageView nextImage = new ImageView("file:../CatchAleksa/src/Assets/Informative/NextImage.png");
			restart.setGraphic(nextImage);
			restart.setBackground(null);
			double xr = x + 419 + 30;
			restart.relocate(xr, y);
			((Group) scene.getRoot()).getChildren().add(restart);

			// Set MouseTransparent to true to allow mouse events to pass through the button
			restart.setOnAction(event -> {
				// Invoke the callback when the button is pressed
				if (restartButtonPressedCallback != null && readyForNext()) {
					restartButtonPressedCallback.run();
				} else {
					Cody cody = new Cody();
					String[] arr = { "You must collect all elusive packages before continuing" };
					cody.say(arr);
					cody.relocate(50, 150);
					((Group) scene.getRoot()).getChildren().add(cody);
				}

			});
			restart.setFocusTraversable(false);

			ImageView hearts = new ImageView(
					"file:../CatchAleksa/src/Assets/Hearts/Hearts" + controller.getHeartsNumber() + ".png");
			double xh = W - (30 + 204);
			hearts.relocate(xh, y + 20);
			((Group) scene.getRoot()).getChildren().add(hearts);
		});

		return scene;
	}

	/**
	 * Checks whether the conditions are met for progressing to the next stage of
	 * the game.
	 *
	 * @return True if the required number of packages is found, false otherwise.
	 */
	private boolean readyForNext() {
		System.out.println("packages needed: " + controller.getPackagesNeeded());
		// controller.getPackagesFoundThisRound();
//		System.out.println((int) controller.getPackagesFoundProperty().getValue());
		return controller.getPackagesNeeded() == (int) controller.getPackagesFoundProperty().getValue();
	}

	/**
	 * Sets the score panel displaying the current stage information.
	 *
	 * @param scene The JavaFX scene to which the score panel will be added.
	 * @param x     The x-coordinate of the score panel.
	 * @param y     The y-coordinate of the score panel.
	 */
	private void setScore(Scene scene, double x, double y) {
		this.score = new ImageView(
				"file:../CatchAleksa/src/Assets/Informative/Stage" + controller.getCurrentRound() + "Info.png");
		score.relocate(x, y);
//		System.out.println("getCurrentRound: " + controller.getCurrentRound());
		((Group) scene.getRoot()).getChildren().add(score);
	}

	public Label getLbl() {

		return labl;
	}
}
