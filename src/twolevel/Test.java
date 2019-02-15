package twolevel;

import java.util.ArrayList;

import bean.User;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<User> test = new ArrayList<User>();
		ArrayList<User> user = new ArrayList<User>();
		
		User u1 = new User(1,"1");
		User u2 = new User(2,"2");
		User u3 = new User(3,"3");
		User t1 = new User(4,"4");
		
		String[] s1 = {"1","1"};
		String[] s2 = {"1","2"};
		String[] s3 = {"2","1"};
		String[] s4 = {"2","2"};
		String[] s5 = {"3","1"};
		
		t1.addPage(s1);
		t1.addPage(s3);
		t1.addPage(s2);
		t1.addPage(s3);
		t1.addPage(s5);
		t1.addPage(s1);
		
		u1.addPage(s1);
		u1.addPage(s2);
		u1.addPage(s3);
		
		u2.addPage(s1);
		u2.addPage(s2);
		u2.addPage(s3);
		
		u3.addPage(s1);
		u3.addPage(s3);
		u3.addPage(s4);
		
		user.add(u1);
		user.add(u2);
		user.add(u3);
		
		test.add(t1);
		
//		Twolevel tl = new Twolevel(test,user);
//		tl.predict();
	}

}
