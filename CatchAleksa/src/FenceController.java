import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The FenceController class manages the creation, movement, and removal of
 * fences in the CatchAleksa game. It provides methods for moving fences
 * horizontally, spawning a row of fences on the lower part of the screen, and
 * clearing fences from the scene. Fences are represented by ImageView objects
 * displaying the "FenceSide.png" image.
 *
 * @author Florin
 */
public class FenceController {
	private static final double W = 1920;
	private static final double H = 1080;

	/**
	 * Moves all fences in the specified scene horizontally with the given speed. If
	 * a fence moves off the screen, its position is reset to the right.
	 *
	 * @param scene The JavaFX scene containing the fences.
	 * @param speed The speed at which the fences move horizontally.
	 * @return The updated JavaFX scene.
	 */
	public Scene moveFences(Scene scene, double speed) {
		ObservableList<Node> children = ((Group) scene.getRoot()).getChildren();
		for (Node node : children) {
			if (node instanceof ImageView) {
				ImageView fenceImageView = (ImageView) node;
				Image fenceImage = fenceImageView.getImage();

				if (fenceImage != null && fenceImage.getUrl().contains("FenceSide.png")) {
					double newX = fenceImageView.getLayoutX() - speed;
					fenceImageView.relocate(newX, fenceImageView.getLayoutY());

					// If the fence moves off the screen, reset its position to the right
					if (newX + fenceImageView.getBoundsInLocal().getWidth() < 0) {
						fenceImageView.relocate(W, fenceImageView.getLayoutY());
					}
				}
			}
		}
		return scene;
	}

	/**
	 * Adds a row of fences to the lower part of the screen in the specified scene.
	 * The fences are represented by ImageView objects with the "FenceSide.png"
	 * image.
	 *
	 * @param scene The JavaFX scene to which the row of fences will be added.
	 * @return The updated JavaFX scene.
	 */
	public Scene addLowerRowOfFences(Scene scene) {
		// Spawn a row of fences on the lower part of the screen
		double fenceWidth = 138;
		double fenceHeight = 95;
		int numberOfFences = 16;

		// Calculate the starting position for the row
		double startX = (W - (numberOfFences * fenceWidth)) / 2; // Centered on the screen
		double startY = H - fenceHeight; // At the bottom of the screen

		for (int i = 0; i < numberOfFences; i++) {
			// Create ImageView for each fence
			ImageView fence = new ImageView(new Image(("Assets/Fences/FenceSide.png")));

			// Position the fence
			double fenceX = startX + i * fenceWidth;
			double fenceY = startY;

			// Set the position
			fence.relocate(fenceX, fenceY);

			((Group) scene.getRoot()).getChildren().add(fence);

		}
		return scene;
	}

	/**
	 * Clears all fences from the specified scene. This method removes ImageView
	 * objects representing fences with the "FenceSide.png" image.
	 *
	 * @param scene The JavaFX scene from which fences will be cleared.
	 * @return The updated JavaFX scene.
	 */
	public Scene clearFences(Scene scene) {
		ObservableList<Node> children = ((Group) scene.getRoot()).getChildren();
		List<Node> fencesToRemove = new ArrayList<>();
		for (Node node : children) {
			if (node instanceof ImageView) {
				ImageView fence = (ImageView) node;
				if (fence.getImage() != null && fence.getImage().getUrl().contains("FenceSide")) {
					// If the node is a fence, add it to the list for removal
					fencesToRemove.add(fence);
				}
			}
		}

		// Remove fences from the scene
		children.removeAll(fencesToRemove);
		return scene;
	}
}
