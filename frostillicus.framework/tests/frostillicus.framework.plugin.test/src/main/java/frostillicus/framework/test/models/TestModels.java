package frostillicus.framework.test.models;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import frostillicus.xsp.model.AbstractModelList;

public class TestModels {

	@Test
	public void testInstantiation() {
		Post.Manager posts = Post.Manager.get();
		assertNotEquals("posts should not be null", null, posts);

		Comment.Manager comments = Comment.Manager.get();
		assertNotEquals("comments should not be null", null, comments);
	}

	@Test
	public void testNewPost() {
		Post.Manager posts = Post.Manager.get();
		assertNotEquals("posts should not be null", null, posts);

		Date posted = new Date();
		{
			Post post = posts.create();
			assertNotEquals("post should not be null", null, post);
			post.setValue("$$Title", "Test Title");
			post.setValue("Posted", posted);
			post.save();
		}
		{
			AbstractModelList<Post> allPosts = posts.getNamedCollection("All", null);
			assertNotEquals("allPosts should not be null", null, allPosts);
			Post post = allPosts.getByKey(posted);
			assertNotEquals("post should not be null", null, post);

			assertTrue("post should be deletable", post.delete());
		}
	}
}
