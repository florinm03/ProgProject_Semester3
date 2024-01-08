import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The FlorinController class manages the gameplay logic and user interaction in
 * the CatchAleksa game. It controls the movement of the player character,
 * handles user input, and orchestrates various game elements. This class is
 * also responsible for managing obstacles, fences, and updating the game state.
 * Extends Thread to handle certain game aspects in a separate thread.
 *
 * @author Florin
 */
public class FlorinController extends Thread {
	private ObstacleController oc = new ObstacleController(this);
	private FenceController fc = new FenceController();
	private GUIController gc = new GUIController(this);
	private volatile boolean isIdle = false;
	private boolean isPhoneAnimationRunning = false;
	private Timeline phoneAnimationTimeline;
	private static final double W = 1920, H = 1080;
	private boolean goNorth, goSouth, goEast, goWest;
	private int upImageIndex = 0;
	private int downImageIndex = 0;
	private int leftImageIndex = 0;
	private int rightImageIndex = 0;
	private boolean isChangingImage = false;
	private AtomicLong lastInteractionTime = new AtomicLong(System.currentTimeMillis());
	private ImageView character;
	private ImageView background = gc.getStartImageView();
	private Scene scene;
	private String playerImage = gc.getplayerImage();
	private static boolean spaceBool = false;
	private boolean fenceAnimationRunning = false;
	private Timeline fenceTimeline;
	private int heartsNumber = 3;
	private Map<ImageView, Timeline> obstacleTimelines = new HashMap<>();
	private Integer packagesFound = 0;
	private int badObstacle = 0;
	private SimpleIntegerProperty packagesFoundProperty = new SimpleIntegerProperty(0);
	private Cody cody;
	public Button restart;
	public ImageView score;
	public Label labl;
	private Duration frameDuration = Duration.millis(200);
	private StringBuffer cheatCodeBuffer = new StringBuffer();
	private int currentRound = 0;
	private int packagesNeeded = 0;
	private int packagesFoundThisRound = 0;
	private static int firstRun = 0;

	// Images
	static List<Image> imagesBack = new ArrayList<>();
	static List<Image> imagesFront = new ArrayList<>();
	static List<Image> imagesLeft = new ArrayList<>();
	static List<Image> imagesRight = new ArrayList<>();

	static List<Image> imagesBushes = new ArrayList<>();
	static List<Image> imagesFences = new ArrayList<>();

	/**
	 * Constructs a new FlorinController and initializes necessary components.
	 */
	public FlorinController() {
		this.character = new ImageView(new Image((playerImage)));
		this.phoneAnimationTimeline = new Timeline();
		packagesFoundProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				updatePackagesLabel(newValue.intValue());
			}
		});

	}

	/**
	 * Starts the game, initializes images, sets up event listeners, and displays
	 * the game window. Creates and displays an introductory message by Cody.
	 */
	public void startGame() {
//		System.out.println("start of startgame");
		imagesBack.add(new Image(("Assets/WalkingAnimation/BackLeft.png")));
		imagesBack.add(new Image(("Assets/WalkingAnimation/BackRight.png")));
		imagesFront.add(new Image(("Assets/WalkingAnimation/FrontLeft.png")));
		imagesFront.add(new Image(("Assets/WalkingAnimation/FrontRight.png")));
		imagesLeft.add(new Image(("Assets/WalkingAnimation/LeftLeft.png")));
		imagesLeft.add(new Image(("Assets/WalkingAnimation/LeftRight.png")));
		imagesRight.add(new Image(("Assets/WalkingAnimation/RightLeft.png")));
		imagesRight.add(new Image(("Assets/WalkingAnimation/RightRight.png")));
		// Methods
		checkIdleTime();
		moveCharacterTo(W / 2, H / 2);
		Group playerGroup = new Group(background, character);

		this.scene = new Scene(playerGroup, W, H, Color.ALICEBLUE);
		// cody intro
		cody = new Cody();
		String[] arr = {
				"Welcome to CatchAleksa, where the virtual world is filled with lies and packet losses! Meet Aleksa, "
						+ "a mysterious being who's stolen packages in transit. Your mission: collect them all." };
		cody.say(arr);
		cody.relocate(420, 50);
		((Group) scene.getRoot()).getChildren().add(cody);

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				keyDOWN(event);
			}
		});

		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				keyUP(event);
			}
		});

		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				int dx = 0, dy = 0;
				// speed
				if (goNorth)
					dy -= 2;
				if (goSouth)
					dy += 2;
				if (goEast)
					dx += 2;
				if (goWest)
					dx -= 2;

				moveCharacterBy(dx, dy);
			}
		};
		timer.start();
	}

	/**
	 * Stops all running timelines, including fence and obstacle animations.
	 */
	private void stopAllTimelines() {
		stopFenceAnimation();
//		stopObstacleAnimations();
	}

	/**
	 * Handles key press events for player movement and other interactions.
	 *
	 * @param event The KeyEvent representing the key press event.
	 */
	private void keyDOWN(KeyEvent event) {
		lastInteractionTime.set(System.currentTimeMillis()); // Update the last interaction time
		if (isIdle && isPhoneAnimationRunning) {
			stopPhoneAnimation(); // Stop phone animation when there is player interaction
		}
		switch (event.getCode()) {
		case UP:
			goNorth = true;
			break;
		case DOWN:
			goSouth = true;
			break;
		case LEFT:
			goWest = true;
			break;
		case RIGHT:
			goEast = true;
			break;
		case SPACE:
			if (isPlayerOnStart(character.getLayoutX(), character.getLayoutY()) && !spaceBool) {
				spaceBool = true;
				// actual game
				startNewGame();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Handles key release events, updating player movement flags and processing
	 * special keys. Shh.. there is an easter egg
	 *
	 * @param event The KeyEvent representing the key release event.
	 */
	private void keyUP(KeyEvent event) {
		switch (event.getCode()) {
		case UP:
			goNorth = false;
			if (true) {
				character.setImage(new Image(("Assets/WalkingAnimation/Back.png")));
			}
			break;
		case DOWN:
			goSouth = false;
			if (true) {
				character.setImage(new Image(("Assets/WalkingAnimation/Front.png")));
			}
			break;
		case LEFT:
			goWest = false;
			if (isChangingImage) {
				character.setImage(new Image(("Assets/WalkingAnimation/Left.png")));
			}
			break;
		case RIGHT:
			goEast = false;
			if (isChangingImage) {
				character.setImage(new Image(("Assets/WalkingAnimation/Right.png")));
			}
			break;
		case ESCAPE:
			cody.quit();
			spaceBool = false;
			fireEvent();
			break;
		case H:
			cheatCodeBuffer.append("H");
			break;
		case E:
			cheatCodeBuffer.append("E");
			break;
		case S:
			cheatCodeBuffer.append("S");
			break;
		case O:
			cheatCodeBuffer.append("O");
			break;
		case Y:
			cheatCodeBuffer.append("Y");
			break;
		case A:
			cheatCodeBuffer.append("A");
			break;
		case M:
			cheatCodeBuffer.append("M");
			break;
		default:
			cheatCodeBuffer.setLength(0);
			break;
		}

		// Check if the cheat code is entered
		if (cheatCodeBuffer.toString().equals("HESOYAM")) {
//			System.out.println("Cheat code activated: HESOYAM");
			setHeartsNumber(4);
			updateHearts();
			// Reset the buffer after successfully entering the cheat code
			cheatCodeBuffer.setLength(0);
		}
	}

	/**
	 * Starts a new game round, clearing obstacles, fences, and updating the game
	 * background. Displays an informational message by Cody and sets up the game
	 * scene.
	 */
	private void startNewGame() {

//		System.out.println("Entered startNewGame()");
		this.incrCurrentRound();
		stopAllTimelines();
		clearObstacles();
		this.scene = fc.clearFences(this.scene);
		// Reset background
		background.setImage(gc.getGameBackground());

		// Move character to starting position
		moveCharacterTo(W / 4, H / 2);
		if (firstRun == 0) {
//			System.out.println("here");
			cody.quit();
			cody = new Cody();
			String[] arr = { "Press NEXT once all packages have been collected." };
			cody.relocate(30, 200);
			cody.say(arr);
			((Group) scene.getRoot()).getChildren().add(cody);
			firstRun++;
		}

		Platform.runLater(() -> {

			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					keyDOWN(event);
				}
			});

			scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					keyUP(event);
				}
			});

		});

		this.scene = gc.addInfoPannels(scene, 10, () -> {
			// Callback function to be executed when the button is pressed
			startNewGame();
			this.setPackagesFoundThisRound(0);
		});
		this.scene = fc.addLowerRowOfFences(this.scene);
		startFenceAnimation();
		oc.spawnObstacles(); // vorher war: spawnObstacles();
	}

	/* ...............Obstacles Methods................. */

	/**
	 * Stops obstacle animations for all obstacles on the screen.
	 */
	private void stopObstacleAnimations() {
		ObservableList<Node> children = ((Group) scene.getRoot()).getChildren();
		for (Node node : children) {
			if (node instanceof ImageView) {
				ImageView obstacle = (ImageView) node;
				if (obstacle.getImage() != null && obstacle.getImage().getUrl().contains("Obstacle")) {
					// If the node is an obstacle, stop its animation and clear the timeline
					stopObstacleAnimation(obstacle);
				}
			}
		}
	}

	/**
	 * Stops the animation for a specific obstacle.
	 *
	 * @param obstacle The ImageView representing the obstacle.
	 */
	private void stopObstacleAnimation(ImageView obstacle) {
		// Retrieve the timeline associated with the obstacle
		Timeline obstacleTimeline = obstacleTimelines.get(obstacle);

		if (obstacleTimeline != null) {
			// Stop the timeline
			obstacleTimeline.stop();

			// Remove the timeline from the map
			obstacleTimelines.remove(obstacle);
		}
	}

	/**
	 * Clears all obstacles from the game scene and stops their animations.
	 */
	private void clearObstacles() {
		ObservableList<Node> children = ((Group) scene.getRoot()).getChildren();
		List<Node> obstaclesToRemove = new ArrayList<>();
		for (Node node : children) {
			if (node instanceof ImageView) {
				ImageView obstacle = (ImageView) node;
				if (obstacle.getImage() != null && obstacle.getImage().getUrl().contains("Obstacle")) {
					// If the node is an obstacle, add it to the list for removal and clear the
					// timeline
					obstaclesToRemove.add(obstacle);
					stopObstacleAnimation(obstacle);
				}
			}
		}

		// Remove obstacles from the scene
		children.removeAll(obstaclesToRemove);
	}

	/**
	 * Checks if the center point of the player is colliding with the image center
	 */
	public boolean isPlayerColliding(ImageView image) {
		Bounds playerBounds = character.getBoundsInParent();
		Bounds imageBounds = image.getBoundsInParent();

		// Calculate the center points
		double playerCenterX = playerBounds.getMinX() + playerBounds.getWidth() / 2.0;
		double playerCenterY = playerBounds.getMinY() + playerBounds.getHeight() / 2.0;

		double imageCenterX = imageBounds.getMinX() + imageBounds.getWidth() / 2.0;
		double imageCenterY = imageBounds.getMinY() + imageBounds.getHeight() / 2.0;

		// Define a threshold for collision
		double collisionThresholdX = (playerBounds.getWidth() + imageBounds.getWidth()) / 4.0;
		double collisionThresholdY = (playerBounds.getHeight() + imageBounds.getHeight()) / 4.0;

		// Check for intersection based on the center points and threshold
		return Math.abs(playerCenterX - imageCenterX) < collisionThresholdX
				&& Math.abs(playerCenterY - imageCenterY) < collisionThresholdY;
	}

	public void startObstacleAnimation(ImageView obstacle, double initialX, double initialY) {

		obstacle.getProperties().put("counted", false);
		obstacle.getProperties().put("countedpackages", false);

		Timeline obstacleTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {
			double obstacleSpeed = 2; // speed of obstacle movement
			double newX = obstacle.getLayoutX() - obstacleSpeed;
			obstacle.relocate(newX, obstacle.getLayoutY());

			// Check for collision with the player
			if (isPlayerColliding(obstacle)) {
				if (obstacle.getImage().getUrl().contains("Obstacle9")
						|| obstacle.getImage().getUrl().contains("Obstacle7")
						|| obstacle.getImage().getUrl().contains("Obstacle8")) {
					// If it's a special obstacle and hasn't been counted yet, increment
					// packagesFound
					if (!(boolean) obstacle.getProperties().get("countedpackages")) {
						packagesFoundProperty.set(packagesFoundProperty.get() + 1);
						this.setPackagesFoundThisRound(this.getPackagesFoundThisRound() + 1);
//						System.out.println(this.getPackagesFoundThisRound());
						obstacle.getProperties().put("countedpackages", true); // Mark as counted
					}
				} else {
					if (!(boolean) obstacle.getProperties().get("counted")) {
						badObstacle++;
//						System.out.println("bad Obastacles touched: " + badObstacle);
						obstacle.getProperties().put("counted", true); // Mark as counted
						updateHearts();
					}
				}
				removeObstacle(obstacle);
			}

			// If the obstacle moves off the screen, reset its position to the right
			if (newX + obstacle.getBoundsInLocal().getWidth() < 0) {
				double resetX = W + 50; // the initial X position of reset obstacles
				obstacle.relocate(resetX, initialY);
			}
		}));
		obstacleTimeline.setCycleCount(Timeline.INDEFINITE);
		obstacleTimeline.play();

		// Store the Timeline associated with the obstacle
		obstacleTimelines.put(obstacle, obstacleTimeline);
	}

	public void removeObstacle(ImageView obstacle) {
		// Stop the obstacle's animation
		stopObstacleAnimation(obstacle);

		// Remove the obstacle from the scene
		((Group) scene.getRoot()).getChildren().remove(obstacle);

		// Remove the obstacle from the map
		obstacleTimelines.remove(obstacle);
	}

	/**
	 * Updates the hearts display based on the current number of remaining lives.
	 * Displays the corresponding heart image.
	 */
	private void updateHearts() {
		heartsNumber--;
		if (heartsNumber >= 0) {
			Platform.runLater(() -> {
				ImageView hearts = new ImageView(
						new Image(("Assets/Hearts/Hearts" + heartsNumber + ".png")));
				double xh = W - (30 + 204);
				double y = 30;
				hearts.relocate(xh, y + 20);
				((Group) scene.getRoot()).getChildren().add(hearts);
			});
		} else {
			// game over

			clearObstacles();
			this.scene = fc.clearFences(scene);
			stopAllTimelines();
			((Group) scene.getRoot()).getChildren().remove(restart);
			((Group) scene.getRoot()).getChildren().remove(labl);
			((Group) scene.getRoot()).getChildren().remove(score);

			cody = new Cody();
			String[] s = {
					"Oh no! The stolen packages remain elusive. Fear not, brave adventurer, for even in defeat, lessons are learned. Return to the digital realm with newfound wisdom and sharpened skills.",
					"Press ESCAPE to exit" };
			cody.relocate(W / 4, H / 2);
			((Group) scene.getRoot()).getChildren().add(cody);
			cody.say(s);
		}
	}

	/* ...............Obstacles Methods............ */

	/* ...............Fences Stuff................. */
	/**
	 * Starts the animation for the moving fences at the bottom of the screen.
	 */
	private void startFenceAnimation() {
		if (!fenceAnimationRunning) {
			fenceTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {
				this.scene = fc.moveFences(this.scene, 2); // the speed of fence movement
			}));
			fenceTimeline.setCycleCount(Timeline.INDEFINITE);
			fenceTimeline.play();
			fenceAnimationRunning = true;
		}
	}

	/**
	 * Stops the animation for the moving fences.
	 */
	private void stopFenceAnimation() {
		if (fenceAnimationRunning) {
			fenceTimeline.stop();
			fenceAnimationRunning = false;
		}
	}
	/* ........................................... */

	private void moveCharacterBy(double dx, double dy) {
		if (dx == 0 && dy == 0)
			return;

		final double cx = character.getBoundsInLocal().getWidth() / 2;
		final double cy = character.getBoundsInLocal().getHeight() / 2;

		double x = cx + character.getLayoutX() + dx;
		double y = cy + character.getLayoutY() + dy;

		// Calculate the length of the movement vector
		double length = Math.sqrt(dx * dx + dy * dy);

		// Normalize the movement vector to have a consistent speed in all directions
		if (length > 0) {
			dx = (dx / length) * 2;
			dy = (dy / length) * 2;
		}

		if (!isChangingImage) {
			if (dy < 0) {
				// Moving UP
				changeCharacterImage(imagesBack.get(upImageIndex), (long) frameDuration.toMillis());
				upImageIndex = (upImageIndex + 1) % imagesBack.size();
			} else if (dy > 0) {
				// Moving DOWN
				changeCharacterImage(imagesFront.get(downImageIndex), (long) frameDuration.toMillis());
				downImageIndex = (downImageIndex + 1) % imagesFront.size();
			} else if (dx < 0) {
				// Moving LEFT
				changeCharacterImage(imagesLeft.get(leftImageIndex), (long) frameDuration.toMillis());
				leftImageIndex = (leftImageIndex + 1) % imagesLeft.size();
			} else if (dx > 0) {
				// Moving RIGHT
				changeCharacterImage(imagesRight.get(rightImageIndex), (long) frameDuration.toMillis());
				rightImageIndex = (rightImageIndex + 1) % imagesRight.size();
			}
		}

		moveCharacterTo(x, y);
	}

	private void changeCharacterImage(Image newImage, long delayMillis) {
		isChangingImage = true;
		character.setImage(newImage);

		Timeline delayTimeline = new Timeline(new KeyFrame(Duration.millis(delayMillis), event -> {
			isChangingImage = false;
		}));
		delayTimeline.setCycleCount(1);
		delayTimeline.play();
	}

	private void moveCharacterTo(double x, double y) {
		final double cx = character.getBoundsInLocal().getWidth() / 2;
		final double cy = character.getBoundsInLocal().getHeight() / 2;

		if (x - cx >= 0 && x + cx <= W && y - cy >= 0 && y + cy <= H) {
			character.relocate(x - cx, y - cy);
		}
	}

	/**
	 * Initiates the phone idle animation when the player is idle for a certain
	 * duration. The idle animation is displayed on the player character.
	 */
	public void phoneAnimation() {
		Image[] idleFrames = new Image[9];
		for (int i = 0; i < 9; i++) {
			idleFrames[i] = new Image(("Assets/IdlePhoneAnimation/IdlePhone" + (i + 1) + ".png"));
		}

		int[] frameIndex = { 0 };

		phoneAnimationTimeline = new Timeline(new KeyFrame(frameDuration, event -> {
			Platform.runLater(() -> {
				if (isIdle) {
					character.setImage(idleFrames[frameIndex[0]]);
					frameIndex[0] = (frameIndex[0] + 1) % 9;
				}
			});
		}));

		phoneAnimationTimeline.setCycleCount(Timeline.INDEFINITE);
		phoneAnimationTimeline.play();
		isPhoneAnimationRunning = true; // Sets the flag to indicate the phone animation is running
	}

	/**
	 * Stops the phone idle animation.
	 */
	public void stopPhoneAnimation() {
		if (phoneAnimationTimeline != null) {
			phoneAnimationTimeline.stop();
			isPhoneAnimationRunning = false; // Sets the flag to indicate the phone animation is not running
		}
	}

	/**
	 * Checks the idle time of the player to trigger the phone idle animation when
	 * idle for a specified duration. Runs as a separate thread to continuously
	 * monitor player activity.
	 */
	public void checkIdleTime() {
		Thread idleChecker = new Thread(() -> {
			long idleThreshold = 10000; // idle threshold in milliseconds (10s)
			while (true) {
				long currentTime = System.currentTimeMillis();
				long timeSinceLastInteraction = currentTime - lastInteractionTime.get();
				if (timeSinceLastInteraction >= idleThreshold) {
					isIdle = true;
					if (!isPhoneAnimationRunning) {
						Platform.runLater(() -> phoneAnimation());

					}
				} else {
					if (isPhoneAnimationRunning) {
						Platform.runLater(() -> stopPhoneAnimation());
					}
					isIdle = false; // The player is not idle
				}

				try {
					Thread.sleep(1000); // Check idle time every 1 second
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		idleChecker.setDaemon(true);
		idleChecker.start();
	}

	/**
	 * Updates the label displaying the number of collected packages.
	 *
	 * @param newValue The new value to be displayed.
	 */
	private void updatePackagesLabel(int newValue) {
		Platform.runLater(() -> {
			// Update the label text with the new packagesFound value
			gc.getLbl().setText(Integer.toString(newValue));
		});
	}

	/**
	 * Checks if the player is on the starting point based on the player's
	 * coordinates.
	 *
	 * @param playerX The X-coordinate of the player.
	 * @param playerY The Y-coordinate of the player.
	 * @return True if the player is on the starting point; otherwise, false.
	 */
	private boolean isPlayerOnStart(double playerX, double playerY) {
		return (playerX > 1550 && playerY > 350 && playerY < 500);
	}

	public void fireEvent() {

		CloseCatchAleksaEvent event = new CloseCatchAleksaEvent();
		Event.fireEvent(scene, event);

	}

	/*-----Getter und Setter-----*/

	public Integer getPackagesFound() {
		return packagesFound;
	}

	public ImageView getCharacter() {
		return character;
	}

	public int getHeartsNumber() {
		return heartsNumber;
	}

	public void setHeartsNumber(int heartsnumber) {
		this.heartsNumber = heartsnumber;
	}

	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public Map getObstacleTimelines() {
		return obstacleTimelines;
	}

	public void setObstacleTimelines(Map obstacleTimelines) {
		this.obstacleTimelines = obstacleTimelines;
	}

	public Cody getCody() {
		return cody;
	}

	public void setCody(Cody cody) {
		this.cody = cody;
	}

	public Label getLabl() {
		return labl;
	}

	public void setLabl(Label labl) {
		this.labl = labl;
	}

	public Button getButton() {
		return restart;
	}

	public ImageView getScoreImage() {
		return score;
	}

	public SimpleIntegerProperty getPackagesFoundProperty() {
		return packagesFoundProperty;
	}

	public void setPackagesFoundProperty(SimpleIntegerProperty packagesFoundProperty) {
		this.packagesFoundProperty = packagesFoundProperty;
	}

	public int getBadObstacle() {
		return badObstacle;
	}

	public void setBadObstacle(int badObstacle) {
		this.badObstacle = badObstacle;
	}

	public int getCurrentRound() {
		if (this.currentRound >= 3)
			this.currentRound = 3;
		return currentRound;

	}

	public void incrCurrentRound() {
		this.currentRound++;
	}

	public void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
		if (this.currentRound >= 3)
			this.currentRound = 3;
	}

	public int getPackagesNeeded() {
		return this.packagesNeeded;
	}

	public void setPackagesNeeded(int number) {
		this.packagesNeeded = number;
	}

	public int getPackagesFoundThisRound() {
		return this.packagesFoundThisRound;
	}

	public void setPackagesFoundThisRound(int number) {
		this.packagesFoundThisRound = number;
	}
}
