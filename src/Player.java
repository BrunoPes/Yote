import java.awt.Color;

class Player {
    private Piece[] availablePieces = new Piece[12];
    private Piece[] ingamePieces = new Piece[12];
    private Color pieceColor;
    private String name;

    public Player(Color pieceColor, String name) {
        this.pieceColor = pieceColor;
        this.name = name;
        for(int i=0; i<12; i++)
            this.availablePieces[i] = new Piece(this.pieceColor);
    }

    public Piece[] getAvailablePieces() {
        return this.availablePieces;
    }

    public Piece getOneAvailablePiece() {
        for(Piece piece : this.availablePieces) {
            if(piece != null) {
                return piece;
            }
        }
        return null;
    }

    public boolean existAnyAvailablePiece(){
        return getOneAvailablePiece() != null ? true : false ;
    }

    public void updatePiecesVectors(Piece removedPiece) {
        for(int i=0; i < 12; i++) {
            if(this.availablePieces[i] == removedPiece) {
                this.availablePieces[i] = null;
//              this.ingamePieces[] = removedPiece;
                return;
            }
        }
        return;
    }

    public void removeKilledPiece(Piece killedPiece) {
        for(int i=0; i < 12; i++) {
            if(this.ingamePieces[i] == killedPiece) {
                this.ingamePieces[i] = null;
                return;
            }
        }

        return;
    }
}
