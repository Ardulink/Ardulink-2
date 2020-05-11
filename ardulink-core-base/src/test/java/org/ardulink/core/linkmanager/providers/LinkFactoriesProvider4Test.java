package org.ardulink.core.linkmanager.providers;

import static org.ardulink.util.anno.LapsedWith.JDK8;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.util.Lists;
import org.ardulink.util.anno.LapsedWith;

public class LinkFactoriesProvider4Test implements LinkFactoriesProvider {

	public static class Executor {

		private final List<LinkFactory<?>> factories;

		public Executor(LinkFactory<?>... factories) {
			this.factories = Arrays.asList(factories);
		}

		@LapsedWith(module = JDK8, value = "Lambda")
		public void execute(LinkFactoriesProvider4Test.Statement statement) throws Exception {
			factories().addAll(factories);
			try {
				statement.execute();
			} finally {
				factories().removeAll(factories);
			}

		}
	}

	public static interface Statement {
		void execute() throws Exception;
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
