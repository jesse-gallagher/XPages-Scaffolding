package org.openntf.xsp.extlib.renderkit;

import java.io.OutputStream;
import java.io.Writer;

import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.faces.render.ResponseStateManager;

import org.openntf.xsp.extlib.Activator;

import com.sun.faces.renderkit.RenderKitImpl;

public class OpenNTFRenderKit extends RenderKitImpl {
	private final static boolean _debug = Activator._debug;
	static {
		if (_debug)
			System.out.println(OpenNTFRenderKit.class.getName() + " loaded");
	}

	public OpenNTFRenderKit() {
		super();
		if (_debug) {
			System.out.println(getClass().getName() + " created");
		}
	}

	@Override
	public void addRenderer(String arg0, String arg1, Renderer arg2) {
		super.addRenderer(arg0, arg1, arg2);
	}

	@Override
	public ResponseStream createResponseStream(OutputStream arg0) {
		return super.createResponseStream(arg0);
	}

	@Override
	public ResponseWriter createResponseWriter(Writer arg0, String arg1, String arg2) {
		return super.createResponseWriter(arg0, arg1, arg2);
	}

	@Override
	public Renderer getRenderer(String arg0, String arg1) {
		return super.getRenderer(arg0, arg1);
	}

	@Override
	public ResponseStateManager getResponseStateManager() {
		return super.getResponseStateManager();
	}

}
