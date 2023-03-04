package org.ardulink.core.linkmanager.providers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.util.Lists;
import org.junit.jupiter.api.function.Executable;

public class LinkFactoriesProvider4Test implements LinkFactoriesProvider {

	public static class Executor {

		private final List<LinkFactory<?>> factories;

		public Executor(LinkFactory<?>... factories) {
			this.factories = Arrays.asList(factories);
		}

		public void execute(Executable statement) throws Throwable {
			factories().addAll(factories);
			try {
				statement.execute();
			} finally {
				factories().removeAll(factories);
			}

		}
	}

	

	private static final ThreadLocal<List<LinkFactory>> factories = new ThreadLocal<List<LinkFactory>>() {
		@Override
		protected List<LinkFactory> initialValue() {
			return Lists.newArrayList();
		}
	};

	private static List<LinkFactory> factories() {
		return factories.get();
	}

	@Override
	public Collection<LinkFactory> loadLinkFactories() {
		return factories();
	}

	public static Executor withRegistered(LinkFactory... factories) {
		return new Executor(factories);
	}

}
