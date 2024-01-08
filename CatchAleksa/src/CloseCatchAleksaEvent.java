import javafx.event.EventType;

public class CloseCatchAleksaEvent extends javafx.event.Event {

	private static final long serialVersionUID = 1L;
	public static final EventType<CloseCatchAleksaEvent> CLOSE_CATCH_ALEKSA_EVENT_TYPE = new EventType<>(ANY,
			"CLOSE_CATCH_ALEKSA");

	public CloseCatchAleksaEvent() {
		super(CLOSE_CATCH_ALEKSA_EVENT_TYPE);
	}

}
