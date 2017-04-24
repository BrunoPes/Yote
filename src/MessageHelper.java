public class MessageHelper {
	private String json;

	public MessageHelper(String message){
		this.json = message;
	}

	public int getPlayer() {
		int start = this.json.indexOf("p:")+2;
		String player = this.json.substring(start, start+1);
		return new Integer(player).intValue();
	}

	public String getAction() {
		int start = this.json.indexOf("a:")+2;
		String move = this.json.substring(start, start+1);
		return move;
	}

	public String getChatMessage() {
		if(this.json.indexOf("s:") >= 0) {
			int start = this.json.indexOf("s:")+2;
			String msg = this.json.substring(start);
			return msg;
		}
		return null;
	}

	public int[] getMovedPos() {
		if(this.json.indexOf("m:") >= 0){
			int start = this.json.indexOf("m:")+2;
			String move = this.json.substring(start, start+2);
			int i = new Integer(move.substring(0, 1)).intValue();
			int j = new Integer(move.substring(1)).intValue();
			return new int[]{i,j};
		}
		return null;
	}

	public int[] getKilledPos() {
		if(this.json.indexOf("k:") >= 0){
			int start = this.json.indexOf("k:")+2;
			String move = this.json.substring(start, start+2);
			int i = new Integer(move.substring(0, 1)).intValue();
			int j = new Integer(move.substring(1)).intValue();
			return new int[]{i,j};
		}
		return null;
	}
}
