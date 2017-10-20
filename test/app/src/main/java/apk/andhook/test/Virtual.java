package apk.andhook.test;

import android.util.Log;

public final class Virtual {
	public static class Virtual0 {
		public int getUserId() {
			Log.i(this.getClass().toString(), "Virtual0 getUserId hit!");
			return 0;
		}
	}

	public static class Virtual1 extends Virtual0 {
		@Override
		public int getUserId() {
			Log.i(this.getClass().toString(), "Virtual1 getUserId hit!");
			return super.getUserId();
		}
	}
	
	public static class Virtual2 extends Virtual1 {
		// we cannot simply replace virtual methods unless they have the same
		// method_index_;
		// however, @Override ensures that
		@Override
		public int getUserId() {
			Log.i(this.getClass().toString(), "faked Virtual1 getUserId hit!");
			return 0;
		}
	}
}
