package core.elements;

import java.util.List;

public class Board {
    private List<BoardElement> boardElements;
    private List<Field> fields;

    public Board(
            List<BoardElement> boardElements,
            List<Field> fields
    ) {
        this.boardElements = boardElements;
        this.fields = fields;
    }

    public List<BoardElement> getBoardElements() { return boardElements; }
    public void setBoardElements(List<BoardElement> boardElements) { this.boardElements = boardElements; }

    public List<Field> getFields() { return fields; }
    public void setFields(List<Field> fields) { this.fields = fields; }
}
