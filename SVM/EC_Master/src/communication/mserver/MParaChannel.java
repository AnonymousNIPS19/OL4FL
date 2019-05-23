package communication.mserver;

import communication.utils.Para;
import io.netty.channel.Channel;


public class MParaChannel {
	public Para paraStoM;
	public Channel socketChannel;
	public MParaChannel(Para paraStoM, Channel socketChannel) {
		this.paraStoM = paraStoM;
		this.socketChannel = socketChannel;
	}

}
