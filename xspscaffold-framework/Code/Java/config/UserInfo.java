package config;

import org.openntf.domino.*;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.directory.DirectoryUser;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.DataObject;

import frostillicus.xsp.bean.ApplicationScoped;
import frostillicus.xsp.bean.ManagedBean;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import frostillicus.xsp.util.FrameworkUtils;

import lotus.domino.NotesException;

@ManagedBean(name="userInfo")
@ApplicationScoped
public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public static UserInfo get() {
		UserInfo instance = (UserInfo)FrameworkUtils.resolveVariable(UserInfo.class.getAnnotation(ManagedBean.class).name());
		return instance == null ? new UserInfo() : instance;
	}

	public boolean isAdmin() {
		return FrameworkUtils.getDatabase().queryAccessRoles(getName()).contains("[Admin]");
	}
	public boolean isDebug() {
		return FrameworkUtils.getDatabase().queryAccessRoles(getName()).contains("[Debug]");
	}

	public String getName() {
		return FrameworkUtils.getUserName();
	}

	@SuppressWarnings("unchecked")
	public Collection<String> getNamesList() throws NotesException {
		Database database = FrameworkUtils.getDatabase();
		Collection<String> names = new TreeSet<String>(new lotus.notes.addins.DominoServer(database.getServer()).getNamesList(FrameworkUtils.getUserName()));
		names.addAll(database.queryAccessRoles(getName()));
		return names;
	}

	public String getDisplayName() {
		DirectoryUser user = ExtLibUtil.getXspContext().getUser();
		List<?> names = FrameworkUtils.getSession().evaluate(" @NameLookup([NoUpdate]; '" + user.getName() + "'; 'FirstName')[1]:@NameLookup([NoUpdate]; '" + user.getName() + "'; 'LastName')[1] ");
		String firstName = names.size() < 1 ? "" : names.get(0).toString();
		String lastName = names.size() < 2 ? "" : names.get(1).toString();
		if(StringUtil.isNotEmpty(firstName)) {
			if(StringUtil.isNotEmpty(lastName)) {
				if(lastName.length() > 1) {
					return firstName + " " + lastName.charAt(0) + ".";
				}
				return firstName + " " + lastName;
			}
			return firstName;
		}
		return user.getCommonName();
	}

	public String getProfileImageUrl() {
		DirectoryUser user = ExtLibUtil.getXspContext().getUser();
		String email = user.getMail();
		return "https://secure.gravatar.com/avatar/" + MD5Util.md5Hex(email);
	}

	public ProfileImages getProfileImages() {
		return ProfileImages.INSTANCE;
	}

	public static enum ProfileImages implements DataObject {
		INSTANCE;


		public Class<DataObject> getType(final Object key) {
			return DataObject.class;
		}

		public DataObject getValue(final Object key) {
			return new DataObject() {

				public Class<String> getType(final Object key2) {
					return String.class;
				}

				public String getValue(final Object key2) {
					return "https://secure.gravatar.com/avatar/" + MD5Util.md5Hex(String.valueOf(key)) + "?s=" + key2;
				}

				public boolean isReadOnly(final Object key2) {
					return true;
				}

				public void setValue(final Object key2, final Object value2) {
					throw new UnsupportedOperationException();
				}
			};
		}

		public boolean isReadOnly(final Object key) {
			return true;
		}

		public void setValue(final Object key, final Object value) {
			throw new UnsupportedOperationException();
		}
	}
}
class MD5Util {
	public static String hex(final byte[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i]
			                                     & 0xFF) | 0x100).substring(1,3));
		}
		return sb.toString();
	}
	public static String md5Hex (final String message) {
		try {
			MessageDigest md =
				MessageDigest.getInstance("MD5");
			return hex (md.digest(message.getBytes("CP1252")));
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
}