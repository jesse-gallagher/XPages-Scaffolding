package org.openntf.xsp.extlib.query;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.util.TypedUtil;
import com.ibm.xsp.util.ValueBindingUtil;

public class ComponentTreeMassage {

	protected static Logger _logger;
	static {
		_logger = Logger.getLogger(ComponentTreeMassage.class.getName());
	}

	public ComponentTreeMassage() {
	}

	public static void removeComponent(UIComponent component) {
		_logger.finer("Remove the " + component.getClass().getCanonicalName() + " component");
		boolean result = component.getParent().getChildren().remove(component);
		_logger.finer("Result of remove is " + result);
	}

	public static void removeComponent(String componentId) {
		_logger.finer("Remove the component with id " + componentId);
		XspQuery domQuery = new XspQuery();
		UIComponent component = domQuery.byId(componentId);
		if (component != null) {
			_logger.finer("Found a component that is of type " + component.getClass().getCanonicalName());
			_logger.finer("Component has an id of " + component.getId() + " and has " + component.getChildren().size() + " children");
			Integer compIndex = component.getParent().getChildren().indexOf(component);
			_logger.finer("component has a parent of " + component.getParent().getId());
			_logger.finer("component is located at index " + compIndex + " out of " + component.getParent().getChildren().size());

			boolean result = component.getParent().getChildren().remove(component);
			_logger.finer("Result of remove is " + result);
		} else {
			_logger.finer("Couldn't find a component with id " + componentId);
		}
	}

	public static void removeDialogBox(UIComponent component) {
		_logger.finer("removeDialogBox running");
		removeChildren(component);
		component.setRendered(false);
		_logger.finer("removeDialogBox done");
	}

	public static void removeChildren(UIComponent component) {
		_logger.finer("start removeChildren");
		try {
			List<UIComponent> children = TypedUtil.getChildren(component);
			if (children.size() > 0) {
				_logger.finer("removChildren " + component.getId() + " has " + component.getChildCount() + " children");
				int counter = 0;
				for (UIComponent child : children) {
					counter++;
					_logger.finer("processing child #" + counter + " of " + component.getId());
					_logger.finer(child.getId() + " has " + child.getChildCount() + " children, calling removeChildren");
					removeChildren(child);
				}
				_logger.finer("out of loop for " + component.getId() + " processed " + counter + " components");
				removeComponent(component);
			} else {
				_logger.finer("removing childless component " + component.getId() + " whose parent is " + component.getParent().getId());
				component.setRendered(false);
				removeComponent(component);
			}
			_logger.finer("finish removeChildren");
		} catch (Exception e) {
			_logger.log(Level.WARNING, "Unhandled Exception", e);
		}
	}

	/**
	 * CAUTION Component Genocide ahead Remove all the components of this type
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void removeComponent(Class componentClass) {
		XspQuery domQuery = new XspQuery();
		List<UIComponent> components = domQuery.addInstanceOf(componentClass).locate();
		if (!components.isEmpty()) {
			for (UIComponent component : components) {
				component.getParent().getChildren().remove(component);
			}
		}
	}

	/**
	 * CAUTION Component Genocide ahead Remove all the components of this type
	 * that are children of the parentComponent
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void removeComponent(Class componentClass, UIComponent parentComponent) {
		XspQuery domQuery = new XspQuery();
		List<UIComponent> components = domQuery.addInstanceOf(componentClass).locate(parentComponent);
		if (!components.isEmpty()) {
			for (UIComponent component : components) {
				component.getParent().getChildren().remove(component);
			}
		}
	}

	/**
	 * CAUTION Component Genocide ahead Remove all the components of this type
	 * that are children of the parentComponent
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void removeComponent(Class componentClass, String parentComponentId) {
		XspQuery domQuery = new XspQuery();
		UIComponent parentComponent = domQuery.byId(parentComponentId);
		if (parentComponent != null) {
			List<UIComponent> components = domQuery.addInstanceOf(componentClass).locate(parentComponent);
			if (!components.isEmpty()) {
				for (UIComponent component : components) {
					component.getParent().getChildren().remove(component);
				}
			}
		}
	}

	public static void addFirst(UIComponent component) {
		TypedUtil.getChildren(component.getParent()).add(0, component);
	}

	public static void addLast(UIComponent component) {
		TypedUtil.getChildren(component.getParent()).add(component);
	}

	public static void addBefore(UIComponent component, UIComponent beforeComp) {
		int beforeCompIndex = TypedUtil.getChildren(beforeComp.getParent()).indexOf(beforeComp);
		TypedUtil.getChildren(beforeComp.getParent()).add(beforeCompIndex, component);
	}

	public static void addBefore(UIComponent component, String beforeCompId) {
		XspQuery domQuery = new XspQuery();
		UIComponent beforeComp = domQuery.byId(beforeCompId);
		if (beforeComp != null) {
			int beforeCompIndex = TypedUtil.getChildren(beforeComp.getParent()).indexOf(beforeComp);
			TypedUtil.getChildren(beforeComp.getParent()).add(beforeCompIndex, component);
		}
	}

	public static void addAfter(UIComponent component, UIComponent afterComp) {
		int afterCompIndex = TypedUtil.getChildren(afterComp.getParent()).indexOf(afterComp) + 1;
		TypedUtil.getChildren(afterComp.getParent()).add(afterCompIndex, component);
	}

	public static void addAfter(UIComponent component, String afterCompId) {
		XspQuery domQuery = new XspQuery();
		UIComponent afterComp = domQuery.byId(afterCompId);
		if (afterComp != null) {
			int afterCompIndex = TypedUtil.getChildren(afterComp.getParent()).indexOf(afterComp) + 1;
			TypedUtil.getChildren(afterComp.getParent()).add(afterCompIndex, component);
		}
	}

	public static void addToLocation(UIComponent component, Integer location) {
		List<UIComponent> siblings = TypedUtil.getChildren(component.getParent());
		if (!siblings.isEmpty()) {
			siblings.add(location, component);
		}
	}

	public static void moveToFirst(UIComponent component) {
		List<UIComponent> siblings = TypedUtil.getChildren(component.getParent());
		if (!siblings.isEmpty()) {
			int movedCompIndex = siblings.indexOf(component);
			UIComponent movedComp = siblings.remove(movedCompIndex);
			siblings.add(0, movedComp);
		}
	}

	public static void moveToLast(UIComponent component) {
		List<UIComponent> siblings = TypedUtil.getChildren(component.getParent());
		if (!siblings.isEmpty()) {
			int movedCompIndex = siblings.indexOf(component);
			UIComponent movedComp = siblings.remove(movedCompIndex);
			siblings.add(movedComp);
		}
	}

	public static void moveToLocation(UIComponent component, Integer location) {
		List<UIComponent> siblings = TypedUtil.getChildren(component.getParent());
		if (!siblings.isEmpty()) {
			int movedCompIndex = siblings.indexOf(component);
			UIComponent movedComp = siblings.remove(movedCompIndex);
			siblings.add(location, movedComp);
		}
	}

	public static void moveToBefore(UIComponent component, UIComponent beforeComp) {
		List<UIComponent> beforeCompSiblings = TypedUtil.getChildren(beforeComp.getParent());
		List<UIComponent> compSiblings = TypedUtil.getChildren(component.getParent());
		if (!beforeCompSiblings.isEmpty()) {
			int movedCompIndex = compSiblings.indexOf(component);
			UIComponent movedComp = compSiblings.remove(movedCompIndex);
			int beforeCompIndex = beforeCompSiblings.indexOf(beforeComp);
			beforeCompSiblings.add(beforeCompIndex, movedComp);
		}
	}

	public static void moveToBefore(UIComponent component, String beforeCompId) {
		XspQuery domQuery = new XspQuery();
		UIComponent beforeComp = domQuery.byId(beforeCompId);
		if (beforeComp != null) {
			List<UIComponent> beforeCompSiblings = TypedUtil.getChildren(beforeComp.getParent());
			List<UIComponent> compSiblings = TypedUtil.getChildren(component.getParent());
			if (!beforeCompSiblings.isEmpty()) {
				int movedCompIndex = compSiblings.indexOf(component);
				UIComponent movedComp = compSiblings.remove(movedCompIndex);
				int beforeCompIndex = beforeCompSiblings.indexOf(beforeComp);
				beforeCompSiblings.add(beforeCompIndex, movedComp);
			}
		}
	}

	public static void moveToAfter(UIComponent component, UIComponent afterComp) {
		List<UIComponent> afterCompSiblings = TypedUtil.getChildren(afterComp.getParent());
		List<UIComponent> compSiblings = TypedUtil.getChildren(component.getParent());
		if (!afterCompSiblings.isEmpty()) {
			int movedCompIndex = compSiblings.indexOf(component);
			UIComponent movedComp = compSiblings.remove(movedCompIndex);
			int afterCompIndex = afterCompSiblings.indexOf(afterComp) + 1;
			afterCompSiblings.add(afterCompIndex, movedComp);
		}
	}

	public static void moveToAfter(UIComponent component, String afterCompId) {
		XspQuery domQuery = new XspQuery();
		UIComponent afterComp = domQuery.byId(afterCompId);
		if (afterComp != null) {
			List<UIComponent> afterCompSiblings = TypedUtil.getChildren(afterComp.getParent());
			List<UIComponent> compSiblings = TypedUtil.getChildren(component.getParent());
			if (!afterCompSiblings.isEmpty()) {
				int movedCompIndex = compSiblings.indexOf(component);
				UIComponent movedComp = compSiblings.remove(movedCompIndex);
				int afterCompIndex = afterCompSiblings.indexOf(afterComp) + 1;
				afterCompSiblings.add(afterCompIndex, movedComp);
			}
		}
	}

	public static void makeChild(UIComponent movedComponent, UIComponent destinationComponent) {
		TypedUtil.getChildren(movedComponent.getParent()).remove(movedComponent);
		TypedUtil.getChildren(destinationComponent).add(movedComponent);
	}

	public static void makeChild(UIComponent movedComponent, String destinationComponentId) {
		XspQuery domQuery = new XspQuery();
		UIComponent destinationComponent = domQuery.byId(destinationComponentId);
		TypedUtil.getChildren(movedComponent.getParent()).remove(movedComponent);
		TypedUtil.getChildren(destinationComponent).add(movedComponent);
	}

	public static void makeChild(String movedComponentId, String destinationComponentId) {
		XspQuery domQuery = new XspQuery();
		UIComponent movedComponent = domQuery.byId(movedComponentId);
		UIComponent destinationComponent = domQuery.byId(destinationComponentId);
		TypedUtil.getChildren(movedComponent.getParent()).remove(movedComponent);
		TypedUtil.getChildren(destinationComponent).add(movedComponent);
	}

	@SuppressWarnings("rawtypes")
	public static void cloneAttribute(UIComponent sourceComponent, UIComponent destinationComponent, String propertyName, Class propertyType) {
		_logger.finest("Cloning " + propertyType.getSimpleName() + " attribute " + propertyName + " from "
				+ sourceComponent.getClass().getSimpleName() + " to " + destinationComponent.getClass().getSimpleName());
		ValueBinding vb = sourceComponent.getValueBinding(propertyName);
		Method getter = getGetter(sourceComponent, propertyName, propertyType);
		Method setter = getSetter(destinationComponent, propertyName, propertyType);
		if (vb == null) {
			cloneAttributeValue(sourceComponent, destinationComponent, getter, setter);
		} else {
			_logger.finest("Cloning value binding " + vb.getExpressionString());
			String expression = vb.getExpressionString();
			if (ValueBindingUtil.isRuntimeExpression(expression)) {
				ValueBinding destinationVb = ApplicationEx.getInstance().createValueBinding(expression);
				destinationComponent.setValueBinding(propertyName, destinationVb);
			} else {
				cloneAttributeValue(sourceComponent, destinationComponent, getter, setter);
			}
		}
	}

	private static void cloneAttributeValue(UIComponent sourceComponent, UIComponent destinationComponent, Method getter, Method setter) {
		if (getter != null) {
			_logger.finest("Getter method is " + getter.getName());
			if (setter != null) {
				_logger.finest("Setter method is " + setter.getName());
				try {
					setter.invoke(destinationComponent, new Object[] { getter.invoke(sourceComponent, (Object[]) null) });
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				_logger.info("Could not locate setter method for destination component");
			}
		} else {
			_logger.info("Could not locate getter method for source component");
		}
	}

	@SuppressWarnings("rawtypes")
	private static Method getGetter(UIComponent component, String propertyName, Class propertyType) {
		Method result = null;
		String prefix = (propertyType.equals(Boolean.class) ? "is" : "get");
		String methodName = getBeanMethodName(prefix, propertyName);
		result = getBeanMethod(component, methodName, null);
		return result;
	}

	@SuppressWarnings("rawtypes")
	private static Method getBeanMethod(Object bean, String methodName, Class propertyType) {
		Method result = null;
		try {
			if (propertyType == null) {
				result = bean.getClass().getMethod(methodName, new Class[] {});
			} else {
				result = bean.getClass().getMethod(methodName, new Class[] { propertyType });
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String getBeanMethodName(String prefix, String propertyName) {
		return prefix + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
	}

	@SuppressWarnings("rawtypes")
	private static Method getSetter(UIComponent component, String propertyName, Class propertyType) {
		Method result = null;
		String methodName = getBeanMethodName("set", propertyName);
		result = getBeanMethod(component, methodName, propertyType);
		return result;
	}

	public static void makeChild(String movedComponentId, UIComponent destinationComponent) {
		XspQuery domQuery = new XspQuery();
		UIComponent movedComponent = domQuery.byId(movedComponentId);
		movedComponent.getParent().getChildren().remove(movedComponent);
		destinationComponent.getChildren().add(movedComponent);
	}
}