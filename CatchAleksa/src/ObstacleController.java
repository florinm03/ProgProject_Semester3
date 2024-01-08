import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The ObstacleController class manages the spawning of obstacles in the
 * CatchAleksa game. It determines the number and type of obstacles to spawn
 * based on the current game level. Obstacles are represented by ImageView
 * objects displaying various obstacle images.
 *
 * @author Florin
 */
public class ObstacleController {

	private static final double W = 1920;
	private static final double H = 1080;
	private FlorinController controller;
	public FenceController fc = new FenceController();

	/**
	 * Constructs a new ObstacleController with a reference to the FlorinController.
	 *
	 * @param controller The FlorinController instance to link with this
	 *                   ObstacleController.
	 */
	public ObstacleController(FlorinController controller) {
		this.controller = controller;
	}

	/**
	 * Spawns obstacles on the screen based on the current game level. The number
	 * and type of obstacles are determined by the current round.
	 */
	public void spawnObstacles() {
		double obstacleWidth = 190;
		double obstacleHeight = 100;
		int columns = 7;
		int rows = 6;
		double columnSpacing = 100;
		double rowSpacing = 60;

		String[] obstacleImagePaths = { "Assets/Obstacles/Obstacle1.png",
				"Assets/Obstacles/Obstacle2.png", "Assets/Obstacles/Obstacle3.png",
				"Assets/Obstacles/Obstacle4.png", "Assets/Obstacles/Obstacle5.png",
				"Assets/Obstacles/Obstacle6.png", "Assets/Obstacles/Obstacle7.png",
				"Assets/Obstacles/Obstacle8.png", "Assets/Obstacles/Obstacle9.png" };

		Random random = new Random();

		// Adjust the number of good obstacles based on the level
		int goodObstaclesCount = 0;
//		System.out.println("current round: " + controller.getCurrentRound());

		if (controller.getCurrentRound() == 1) {
			goodObstaclesCount = 7;
		} else if (controller.getCurrentRound() == 2) {
			goodObstaclesCount = 5;
		} else if (controller.getCurrentRound() == 3) {
			goodObstaclesCount = 3;
		}

		// Randomize the positions of good obstacles
		List<Integer> positions = new ArrayList<>();
		for (int i = 0; i < columns * rows; i++) {
			positions.add(i);
		}
		Collections.shuffle(positions);

		// Spawn obstacles based on the randomized positions
		int goodObstaclesSpawned = 0;
		for (int position : positions) {
			int row = position / columns;
			int col = position % columns;

			String obstacleImagePath;
			if (goodObstaclesSpawned < goodObstaclesCount) {
				// Randomly select one of the good obstacle images
				obstacleImagePath = obstacleImagePaths[6 + random.nextInt(3)]; // Index 6, 7, 8
				goodObstaclesSpawned++;
				controller.setPackagesNeeded(controller.getPackagesNeeded() + 1);
			} else {
				// Randomly select one of the other obstacle images
				obstacleImagePath = obstacleImagePaths[random.nextInt(obstacleImagePaths.length - 3)];
			}

			// Create ImageView for each obstacle
			ImageView obstacle = new ImageView(new Image(obstacleImagePath));
			if (!obstacleImagePath.equals("Assets/Obstacles/Obstacle9.png")
					&& !obstacleImagePath.equals("Assets/Obstacles/Obstacle7.png")
					&& !obstacleImagePath.equals("Assets/Obstacles/Obstacle8.png")) {
				obstacle.setFitWidth(obstacleWidth);
				obstacle.setFitHeight(obstacleHeight);
			}

			// Position the obstacle
			double obstacleX = W + col * (obstacleWidth + columnSpacing);
			double obstacleY = H - obstacleHeight - 100 - row * (obstacleHeight + rowSpacing);

			// Set the position
			obstacle.relocate(obstacleX, obstacleY);
			Scene scene = controller.getScene();
			// Add the obstacle to the scene
			((Group) scene.getRoot()).getChildren().add(obstacle);
			controller.setScene(scene);
			controller.startObstacleAnimation(obstacle, obstacleX, obstacleY);
		}
	}
}