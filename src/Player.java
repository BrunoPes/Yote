class Player {
	private Piece[] playerPieces = new Piece[12];
	private String pieceColor;
	private String name;

	public Player(String pieceColor, String name) {
		this.pieceColor = pieceColor;
		this.name = name;
		for(int i=0; i<12; i++)
			this.playerPieces[i] = new Piece(this.pieceColor);
	}

	public void removeKilledPiece(Piece killedPiece) {
		for(int i=0; i < 12; i++) {
			if(this.playerPieces[i] == killedPiece) {
				this.playerPieces[i] = null;
				return;
			}
		}
		return;
	}
}