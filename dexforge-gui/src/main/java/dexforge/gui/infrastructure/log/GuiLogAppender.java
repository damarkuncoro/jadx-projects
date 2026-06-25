package dexforge.gui.infrastructure.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.function.Consumer;

/**
 * Logback Appender that redirects log events to a GUI consumer.
 */
public final class GuiLogAppender extends AppenderBase<ILoggingEvent> {
	private static Consumer<String> logConsumer;

	public static void setLogConsumer(Consumer<String> consumer) {
		logConsumer = consumer;
	}

	@Override
	protected void append(ILoggingEvent event) {
		if (logConsumer != null) {
			logConsumer.accept(event.getFormattedMessage());
		}
	}
}
