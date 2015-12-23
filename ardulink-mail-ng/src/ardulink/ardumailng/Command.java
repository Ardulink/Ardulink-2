package ardulink.ardumailng;

import com.github.pfichtner.ardulink.core.Link;

public interface Command {

	void execute(Link link) throws Exception;

}
