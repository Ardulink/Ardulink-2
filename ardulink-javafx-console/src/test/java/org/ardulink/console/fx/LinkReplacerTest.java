package org.ardulink.console.fx;

import static org.ardulink.core.NullLink.NULL_LINK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.ardulink.core.ConnectionListener;
import org.ardulink.core.Link;
import org.junit.jupiter.api.Test;

class LinkReplacerTest {

	@Test
	void whenNewLinkIsNullConnectionLostIsCalled() {
		ConnectionListener listener = mock(ConnectionListener.class);
		LinkReplacer.replace(NULL_LINK, NULL_LINK, listener);
		verify(listener).connectionLost();
		verifyNoMoreInteractions(listener);
	}

	@Test
	void whenNewLinkIsNotNullReconnectedIsCalled() {
		ConnectionListener listener = mock(ConnectionListener.class);
		LinkReplacer.replace(NULL_LINK, mock(Link.class), listener);
		verify(listener).reconnected();
		verifyNoMoreInteractions(listener);
	}

	@Test
	void whenReplacingOldWithNullConnectionLostIsCalledNotReconnected() {
		ConnectionListener listener = mock(ConnectionListener.class);
		LinkReplacer.replace(mock(Link.class), NULL_LINK, listener);
		verify(listener).connectionLost();
		verifyNoMoreInteractions(listener);
	}

	@Test
	void returnsNewLink() {
		Link newLink = mock(Link.class);
		Link result = LinkReplacer.replace(NULL_LINK, newLink, mock(ConnectionListener.class));
		assertThat(result).isSameAs(newLink);
	}
}
