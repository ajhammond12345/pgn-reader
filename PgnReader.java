import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class PgnReader {
    private static int movesMade;
    public static void main(String[] args) {
        movesMade = 0;
        String game = fileContent(args[0]);
        System.out.format("Event: %s%n", tagValue("Event", game));
        System.out.format("Site: %s%n", tagValue("Site", game));
        System.out.format("Date: %s%n", tagValue("Date", game));
        System.out.format("Round: %s%n", tagValue("Round", game));
        System.out.format("White: %s%n", tagValue("White", game));
        System.out.format("Black: %s%n", tagValue("Black", game));
        System.out.format("Result: %s%n", tagValue("Result", game));
        System.out.println("Final Position:");
        System.out.println(finalPosition(game));
    }

    public static String fileContent(String path) {
        Path file = Paths.get(path);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                // Add the \n that's removed by readline()
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
            System.exit(1);
        }
        return sb.toString();
    }
    public static String tagValue(String tagName, String game) {
        Scanner scan = new Scanner(game);
        while (scan.hasNextLine()) {
            String newLine = scan.nextLine();
            if (newLine.contains(tagName)) {
                int firstQuote = newLine.indexOf("\"");
                String tmp = newLine.substring(firstQuote + 1,
                                                newLine.length());
                //since creating tmp removes firstQuote + 1 number of letters,
                //must add firstQuote + 1 to find the true index
                int secondQuote = tmp.indexOf("\"") + firstQuote + 1;
                return newLine.substring(firstQuote + 1, secondQuote);
            }
        }
        return "NOT GIVEN";
    }

    public static String finalPosition(String game) {
        String fullTextGameMoves = getGameText(game);
        //System.out.println("\nfullTextGameMoves:\n\n" + fullTextGameMoves);
        fullTextGameMoves = replaceNewLines(fullTextGameMoves);
        String[] gameMoves = fullTextGameMoves.split(" ");
        String[][] gameState = makeStartingGameState();
        //printGame(gameState);
        for (int i = 0; i < gameMoves.length; i++) {
            if (i % 3 != 0) {
                //System.out.println("This is the game move: " + gameMoves[i]);
                boolean isWhite;
                if (i % 3 == 1) {
                    isWhite = true;
                } else {
                    isWhite = false;
                }
                gameState = interpretMove(gameMoves[i], gameState, isWhite);
                //printGame(gameState);
            }
            //Since the move number is never removed from array,
            //every 3rd index represents the next black/white move, with
            //%3 = 0 representing the move number,
            //%3 = 1 representing white,
            //%3 = 2 representing black
        }
        //System.out.println("\n\n\n\n\n\nGAME OVER\n");
        //printGame(gameState);
        String result = convertGameToFen(gameState);
        return result;
    }

    private static String convertGameToFen(String[][] game) {
        String result = "";
        for (int i = 0; i < game.length; i++) {
            int count = 0;
            for (int j = 0; j < game[i].length; j++) {
                if (game[i][j].equals("  ")) {
                    count++;
                } else {
                    if (count > 0) {
                        result = result + count;
                        count = 0;
                    }
                    char piece = game[i][j].charAt(1);
                    if (game[i][j].charAt(0) == 'b') {
                        piece = (char) ((piece - 'A') + 'a');
                    }
                    result = result + piece;
                }
            }
            if (count > 0) {
                result = result + count;
                count = 0;
            }
            if (i < (game.length - 1)) {
                result += "/";
            }
        }
        return result;
    }

    private static String replaceNewLines(String text) {
        while (text.contains("\n")) {
            int index = text.indexOf("\n");
            text = text.substring(0, index) + " " + text.substring(index + 1,
                    text.length());
        }
        return text;
    }

    private static String getGameText(String game) {
        int index = game.indexOf("1. ");
        if (index == -1) {
            return "NOT GIVEN";
        } else {
            return game.substring(index, game.length());
        }
    }
    private static String[][] makeStartingGameState() {
        String[][] blankGameState = {
            {"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"},
            {"bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP"},
            {"  ", "  ", "  ", "  ", "  ", "  ", "  ", "  "},
            {"  ", "  ", "  ", "  ", "  ", "  ", "  ", "  "},
            {"  ", "  ", "  ", "  ", "  ", "  ", "  ", "  "},
            {"  ", "  ", "  ", "  ", "  ", "  ", "  ", "  "},
            {"wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP"},
            {"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR"}
        };
        return blankGameState;
    }
    private static void printGame(String[][] gameState) {
        for (int i = 0; i < gameState.length; i++) {
            System.out.print(gameState.length - i + "  ");
            for (int j = 0; j < gameState[i].length; j++) {
                System.out.print(gameState[i][j]);
                if (j < gameState[i].length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.print("\n");
        }
        System.out.println("   A  B  C  D  E  F  G  H \n\n\n");
    }

    private static String removeCharacter(String c, String move) {
        while (move.contains(c)) {
            int charIndex = move.indexOf(c);
            move = move.substring(0, charIndex) + move.substring(charIndex + 1,
                    move.length());
        }
        return move;
    }

    private static String[][] interpretMove(String move, String[][] gameState,
            boolean isWhite) {
        //if the move number gets passed in, this ignores it
        if (move.contains(".")) {
            return gameState;
        }
        if (Character.isDigit(move.charAt(0))) {
            return gameState;
        }
        boolean isTaking;
        if (move.contains("x")) {
            int xIndex = move.indexOf("x");
            move = move.substring(0, xIndex) + move.substring(xIndex + 1,
                    move.length());
            isTaking = true;
        } else {
            isTaking = false;
        }
        String promotion = "";
        if (move.contains("=")) {
            int equalIndex = move.indexOf("=");
            promotion = move.substring(equalIndex + 1, equalIndex + 2);
            move = move.substring(0, equalIndex);
        }
        //removes extraneous information
        move = removeCharacter("=", move);
        move = removeCharacter("#", move);
        move = removeCharacter("+", move);
        move = removeCharacter("?", move);
        move = removeCharacter("!", move);
        move = removeCharacter("\n", move);
        move = removeCharacter("-", move);
        //checks to see what type of piece is being moved
        char[] disambiguation = new char[0];
        char file;
        char rank;
        char firstCharacter = move.charAt(0);
        if (Character.isUpperCase(firstCharacter) && firstCharacter != 'O') {
            if (move.length() > 3) {
                //single character disambiguation
                if (move.length() == 4) {
                    disambiguation = new char[1];
                    disambiguation[0] = move.charAt(1);
                    move = move.substring(0, 1) + move.substring(2,
                            move.length());
                }
                if (move.length() == 5) {
                    disambiguation = new char[2];
                    disambiguation[0] = move.charAt(1);
                    disambiguation[1] = move.charAt(2);
                    move = move.substring(0, 1) + move.substring(3,
                            move.length());
                }
            }
            file = move.charAt(1);
            rank = move.charAt(2);
        } else if (firstCharacter != 'O') {
            if (move.length() > 2) {
                disambiguation = new char[1];
                disambiguation[0] = move.charAt(0);
                move = move.substring(1, move.length());
            }
            file = move.charAt(0);
            rank = move.charAt(1);
            firstCharacter = move.charAt(0);
        } else {
            file = 'i';
            rank = '9';
        }
        int fileIndex = file - 'a';
        int rankIndex = 8 - (rank - '0');
        //System.out.println("Move: " + move + " FileInt: " + fileIndex
        //+ "RankInt: " + rankIndex);
        switch (firstCharacter) {
        case 'B':   gameState = bishopMove(fileIndex, rankIndex, gameState,
                            isWhite, isTaking, disambiguation);
                    break;
        case 'N':   gameState = knightMove(fileIndex, rankIndex, gameState,
                            isWhite, isTaking, disambiguation);
                    break;
        case 'R':   gameState = rookMove(fileIndex, rankIndex, gameState,
                            isWhite, isTaking, disambiguation);
                    break;
        case 'Q':   gameState = queenMove(fileIndex, rankIndex, gameState,
                            isWhite, isTaking, disambiguation);
                    break;
        case 'K':   gameState = kingMove(fileIndex, rankIndex, gameState,
                            isWhite, isTaking, disambiguation);
                    break;
        //PGN format allows for pawns to be noted as P
        case 'P':   gameState = pawnMove(fileIndex, rankIndex, gameState,
                            isWhite, isTaking, disambiguation, promotion);
                    break;
        case 'O':   gameState = castleMove(move, gameState,
                            isWhite, isTaking, disambiguation);
                    break;
        default:    gameState = pawnMove(fileIndex, rankIndex, gameState,
                            isWhite, isTaking, disambiguation, promotion);
                    break;
        }
        return gameState;
    }

    private static String[][] bishopMove(int fileIndex, int rankIndex,
            String[][] gameState, boolean isWhite, boolean isTaking,
            char[] disambiguation) {
        int[][] bishopPositions = findPieceLocations('B', gameState, isWhite);
        for (int i = 0; i < bishopPositions.length; i++) {
            //only works if no pawn becomes a bishop, need to change
            if (((bishopPositions[i][0] + bishopPositions[i][1]) % 2)
                    == ((rankIndex + fileIndex) % 2)) {
                int oldRank = bishopPositions[i][0];
                int oldFile = bishopPositions[i][1];
                gameState = movePiece(oldRank, oldFile, rankIndex, fileIndex,
                        gameState);
                return gameState;
            }
        }
        return gameState;
    }

    private static String[][] knightMove(int fileIndex, int rankIndex,
            String[][] gameState, boolean isWhite, boolean isTaking,
            char[] disambiguation) {
        int[][] knightLocation = findPieceLocations('N', gameState, isWhite);
        boolean[] canBeKnight = new boolean[knightLocation.length];
        int countOfValidKnights = 0;
        for (int i = 0; i < knightLocation.length; i++) {
            int rankDif = rankIndex - knightLocation[i][0];
            int fileDif = fileIndex - knightLocation[i][1];
            if ((((rankDif == 2) || (rankDif == -2)) && ((fileDif == 1)
                            || fileDif == -1)) || (((rankDif == 1)
                                || (rankDif == -1)) && ((fileDif == 2)
                                || fileDif == -2))) {
                if (!moveLeavesKingInCheck(knightLocation[i][0],
                            knightLocation[i][1], rankIndex, fileIndex,
                            gameState, isWhite)) {
                    canBeKnight[i] = true;
                    countOfValidKnights++;
                }
            } else {
                canBeKnight[i] = false;
            }
        }
        if (countOfValidKnights > 1) {
            if (disambiguation.length == 1) {
                if (Character.isDigit(disambiguation[0])) {
                    int disambiguationRankIndex = 8 - disambiguation[1] - '0';
                    for (int i = 0; i < knightLocation.length; i++) {
                        if (knightLocation[i][0] == disambiguationRankIndex
                                && canBeKnight[i]) {
                            gameState = movePiece(knightLocation[i][0],
                                    knightLocation[i][1], rankIndex, fileIndex,
                                    gameState);
                        }
                    }
                } else {
                    int disambiguationFileIndex = disambiguation[0] - 'a';
                    for (int i = 0; i < knightLocation.length; i++) {
                        if (knightLocation[i][1] == disambiguationFileIndex
                                && canBeKnight[i]) {
                            gameState = movePiece(knightLocation[i][0],
                                    knightLocation[i][1], rankIndex, fileIndex,
                                    gameState);
                        }
                    }
                }
            } else if (disambiguation.length == 2) {
                int disambiguationRankIndex = 8 - disambiguation[1] - '0';
                int disambiguationFileIndex = disambiguation[0] - 'a';
                gameState = movePiece(disambiguationRankIndex,
                        disambiguationFileIndex, rankIndex, fileIndex,
                        gameState);
            }

        } else {
            for (int i = 0; i < canBeKnight.length; i++) {
                if (canBeKnight[i]) {
                    int knightRank = knightLocation[i][0];
                    int knightFile = knightLocation[i][1];
                    gameState = movePiece(knightLocation[i][0],
                            knightLocation[i][1], rankIndex, fileIndex,
                            gameState);
                }
            }
        }
        return gameState;
    }

    private static String[][] rookMove(int fileIndex, int rankIndex,
            String[][] gameState, boolean isWhite, boolean isTaking,
            char[] disambiguation) {
        int[][] rookLocation = findPieceLocations('R', gameState, isWhite);
        if (rookLocation.length == 1) {
            gameState = movePiece(rookLocation[0][0], rookLocation[0][1],
                    rankIndex, fileIndex, gameState);
        } else {
            if (disambiguation.length == 0) {
                for (int i = 0; i < rookLocation.length; i++) {
                    int rookRank = rookLocation[i][0];
                    int rookFile = rookLocation[i][1];
                    if (isValidRookMove(rookRank, rookFile, rankIndex,
                                fileIndex, gameState, isWhite)) {
                        gameState = movePiece(rookRank, rookFile, rankIndex,
                                fileIndex, gameState);
                    }
                }
            } else if (disambiguation.length == 1) {
                boolean isRank = Character.isDigit(disambiguation[0]);
                if (isRank) {
                    int disambiguationRankIndex = 8 - (disambiguation[1] - '0');
                    for (int i = 0; i < rookLocation.length; i++) {
                        if (rookLocation[i][0] == disambiguationRankIndex) {
                            if (isValidRookMove(rookLocation[i][0],
                                        rookLocation[i][1], rankIndex,
                                        fileIndex, gameState, isWhite)) {
                                gameState = movePiece(rookLocation[i][0],
                                        rookLocation[i][1], rankIndex,
                                        fileIndex, gameState);
                                return gameState;
                            }
                        }
                    }
                } else {
                    int disambiguationFileIndex = disambiguation[0] - 'a';
                    for (int i = 0; i < rookLocation.length; i++) {
                        if (rookLocation[i][1] == disambiguationFileIndex) {
                            if (isValidRookMove(rookLocation[i][0],
                                        rookLocation[i][1], rankIndex,
                                        fileIndex, gameState, isWhite)) {
                                gameState = movePiece(rookLocation[i][0],
                                        rookLocation[i][1], rankIndex,
                                        fileIndex, gameState);
                                return gameState;
                            }
                        }
                    }
                }
            } else if (disambiguation.length == 2) {
                //disambiguation array goes file then rank
                int disambiguationRankIndex = 8 - (disambiguation[1] - '0');
                int disambiguationFileIndex = disambiguation[0] - 'a';
                gameState = movePiece(disambiguationRankIndex,
                        disambiguationFileIndex, rankIndex, fileIndex,
                        gameState);
            }
        }
        return gameState;
    }

    private static String[][] castleMove(String move, String[][] gameState,
            boolean isWhite, boolean isTaking, char[] disambiguation) {
        if (move.length() == 2) {
            if (isWhite) {
                gameState = movePiece(7, 4, 7, 6, gameState);
                gameState = movePiece(7, 7, 7, 5, gameState);
            } else {
                gameState = movePiece(0, 4, 0, 6, gameState);
                gameState = movePiece(0, 7, 0, 5, gameState);
            }
        }
        if (move.length() == 3) {
            if (isWhite) {
                gameState = movePiece(7, 4, 7, 2, gameState);
                gameState = movePiece(7, 0, 7, 3, gameState);
            } else {
                gameState = movePiece(0, 4, 0, 2, gameState);
                gameState = movePiece(0, 0, 0, 3, gameState);
            }
        }
        return gameState;
    }

    private static String[][] queenMove(int fileIndex, int rankIndex,
            String[][] gameState, boolean isWhite, boolean isTaking,
            char[] disambiguation) {
        int[][] queenLocation = findPieceLocations('Q', gameState, isWhite);
        if (queenLocation.length == 1) {
            gameState = movePiece(queenLocation[0][0], queenLocation[0][1],
                    rankIndex, fileIndex, gameState);
        } else {
            if (disambiguation.length == 0) {
                //fine the queen on the board that's able to hit
                for (int i = 0; i < queenLocation.length; i++) {
                    int queenRank = queenLocation[i][0];
                    int queenFile = queenLocation[i][1];
                    if (isValidQueenMove(queenRank, queenFile, rankIndex,
                                fileIndex, gameState, isWhite)) {
                        gameState = movePiece(queenRank, queenFile, rankIndex,
                                fileIndex, gameState);
                    }
                }
            } else if (disambiguation.length == 1) {
                boolean isRank = Character.isDigit(disambiguation[0]);
                if (isRank) {
                    int disambiguationRankIndex = 8 - (disambiguation[1] - '0');
                    for (int i = 0; i < queenLocation.length; i++) {
                        if (queenLocation[i][0] == disambiguationRankIndex) {
                            if (isValidQueenMove(queenLocation[i][0],
                                        queenLocation[i][1], rankIndex,
                                        fileIndex, gameState, isWhite)) {
                                gameState = movePiece(queenLocation[i][0],
                                        queenLocation[i][1], rankIndex,
                                        fileIndex, gameState);
                                return gameState;
                            }
                        }
                    }
                } else {
                    int disambiguationFileIndex = disambiguation[0] - 'a';
                    for (int i = 0; i < queenLocation.length; i++) {
                        if (queenLocation[i][1] == disambiguationFileIndex) {
                            if (isValidQueenMove(queenLocation[i][0],
                                        queenLocation[i][1], rankIndex,
                                        fileIndex, gameState, isWhite)) {
                                gameState = movePiece(queenLocation[i][0],
                                        queenLocation[i][1], rankIndex,
                                        fileIndex, gameState);
                                return gameState;
                            }
                        }
                    }
                }
            } else if (disambiguation.length == 2) {
                //disambiguation array goes file then rank
                int disambiguationRankIndex = 8 - (disambiguation[1] - '0');
                int disambiguationFileIndex = disambiguation[0] - 'a';
                gameState = movePiece(disambiguationRankIndex,
                        disambiguationFileIndex, rankIndex, fileIndex,
                        gameState);
            }
        }
        return gameState;
    }

    private static String[][] kingMove(int fileIndex, int rankIndex,
            String[][] gameState, boolean isWhite, boolean isTaking,
            char[] disambiguation) {
        int[][] kingLocation = findPieceLocations('K', gameState, isWhite);
        gameState = movePiece(kingLocation[0][0], kingLocation[0][1],
                rankIndex, fileIndex, gameState);
        return gameState;
    }

    private static String[][] pawnMove(int fileIndex, int rankIndex,
            String[][] gameState, boolean isWhite, boolean isTaking,
            char[] disambiguation, String promotion) {
        //pawn locations are stored rank, file
        int[][] pawnLocations = findPieceLocations('P', gameState, isWhite);
        if (!isTaking) {
            if (isWhite) {
                //less than 5 because orientation flipped, i.e. counting up
                //in a loop counts towards white
                for (int i = rankIndex; i <= 6; i++) {
                    if (gameState[i][fileIndex].equals("wP")) {
                        gameState = movePawn(i, fileIndex, rankIndex,
                                fileIndex, gameState, isWhite, promotion);
                        return gameState;
                    }
                }
            } else {
                for (int i = rankIndex; i >= 1; i--) {
                    if (gameState[i][fileIndex].equals("bP")) {
                        gameState = movePawn(i, fileIndex, rankIndex,
                                fileIndex, gameState, isWhite, promotion);
                        return gameState;
                    }
                }
            }
        } else {
            int pawnMustBeOnRank;
            //is en passant
            if (gameState[rankIndex][fileIndex].equals("  ")) {
                if (isWhite) {
                    gameState[rankIndex + 1][fileIndex] = "  ";
                } else {
                    gameState[rankIndex - 1][fileIndex] = "  ";
                }
            }
            if (isWhite) {
                pawnMustBeOnRank = rankIndex + 1;
            } else {
                pawnMustBeOnRank = rankIndex - 1;
            }
            if (disambiguation.length > 0) {
                char disambig = disambiguation[0];
                int disambiguationFile = disambig - 'a';
                gameState = movePawn(pawnMustBeOnRank, disambiguationFile,
                        rankIndex, fileIndex, gameState, isWhite, promotion);
                return gameState;
            }
            for (int i = 0; i < pawnLocations.length; i++) {
                int pawnRank = pawnLocations[i][0];
                int pawnFile = pawnLocations[i][1];
                if ((pawnRank == pawnMustBeOnRank)
                            && ((pawnFile == (fileIndex + 1))
                                || (pawnFile == (fileIndex - 1)))) {
                    gameState = movePawn(pawnRank, pawnFile, rankIndex,
                            fileIndex, gameState, isWhite, promotion);
                    return gameState;
                }
            }
        }
        return gameState;
    }

    //this method will return a 2D array where each entry is the the locations
    //of the piece in row,column format
    private static int[][] findPieceLocations(char pieceType,
            String[][] gameState, boolean isWhite) {
        char whiteBlack;
        if (isWhite) {
            whiteBlack = 'w';
        } else {
            whiteBlack = 'b';
        }
        String searchString = "" + whiteBlack + pieceType;
        int[] rowLocations = new int[8]; //max of 8 pawns
        int[] columnLocations = new int[8];
        int count = 0;

        for (int i = 0; i < gameState.length; i++) {
            for (int j = 0; j < gameState[i].length; j++) {
                if (gameState[i][j].equals(searchString)) {
                    rowLocations[count] = i;
                    columnLocations[count] = j;
                    count++;
                }
            }
        }
        int[][] result = new int[count][2];
        for (int i = 0; i < count; i++) {
            result[i][0] = rowLocations[i];
            result[i][1] = columnLocations[i];
        }

        return result;
    }

    private static String[][] movePiece(int startRank, int startFile,
            int endRank, int endFile, String[][] gameState) {
        gameState[endRank][endFile] = gameState[startRank][startFile];
        gameState[startRank][startFile] = "  ";
        movesMade++;
        return gameState;
    }

    private static String[][] movePawn(int startRank, int startFile,
            int endRank, int endFile, String[][] gameState, boolean isWhite,
            String promotion) {
        gameState[endRank][endFile] = gameState[startRank][startFile];
        gameState[startRank][startFile] = "  ";
        if (promotion.length() > 0) {
            String whiteOrBlack;
            if (isWhite) {
                whiteOrBlack = "w";
            } else {
                whiteOrBlack = "b";
            }
            String newPiece = whiteOrBlack + promotion;
            gameState[endRank][endFile] = newPiece;
        }
        movesMade++;
        return gameState;
    }


    private static boolean isValidQueenMove(int queenRank, int queenFile,
            int moveRank, int moveFile, String[][] gameState,
            boolean isWhite) {
        if (isValidRookMove(queenRank, queenFile, moveRank, moveFile,
                    gameState, isWhite)
                || isValidBishopMove(queenRank, queenFile, moveRank, moveFile,
                    gameState, isWhite)) {
            return true;
        }
        return false;
    }

    private static boolean isValidRookMove(int rookRank, int rookFile,
            int moveRank, int moveFile, String[][] gameState,
            boolean isWhite) {
        if (rookRank == moveRank) {
            if (rookFile > moveFile) {
                for (int i = rookFile - 1; i > moveFile; i--) {
                    if (!gameState[rookRank][i].equals("  ")) {
                        return false;
                    }
                }
            } else {
                for (int i = rookFile + 1; i < moveFile; i++) {
                    if (!gameState[rookRank][i].equals("  ")) {
                        return false;
                    }
                }
            }
        } else if (rookFile == moveFile) {
            if (rookRank > moveRank) {
                for (int i = rookRank - 1; i > moveRank; i--) {
                    if (!gameState[i][rookFile].equals("  ")) {
                        return false;
                    }
                }
            } else {
                for (int i = rookRank + 1; i < moveRank; i++) {
                    if (!gameState[i][rookFile].equals("  ")) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        if (moveLeavesKingInCheck(rookRank, rookFile, moveRank, moveFile,
                    gameState, isWhite)) {
            return false;

        }
        return true;
    }

    private static boolean isValidBishopMove(int bishopRank, int bishopFile,
            int moveRank, int moveFile, String[][] gameState,
            boolean isWhite) {
        int rankDifference = moveRank - bishopRank;
        int fileDifference = moveFile - bishopFile;
        if (Math.abs(rankDifference) != Math.abs(fileDifference)) {
            return false;
        }
        int rMulti = rankDifference / Math.abs(rankDifference);
        int fMulti = fileDifference / Math.abs(fileDifference);
        for (int i = 0; i < Math.abs(rankDifference); i++) {
            if (!gameState[bishopRank + (i * rMulti)][bishopFile + (i * fMulti)]
                    .equals("  ")) {
                return false;
            }
        }
        if (moveLeavesKingInCheck(bishopRank, bishopFile, moveRank, moveFile,
                    gameState, isWhite)) {
            return false;
        }
        return true;
    }

    private static boolean moveLeavesKingInCheck(int startRank, int startFile,
            int moveRank, int moveFile, String[][] gameState,
            boolean isWhite) {
        String[][] tmpGameState = new String[gameState.length][0];
        for (int i = 0; i < gameState.length; i++) {
            tmpGameState[i] = new String[gameState[i].length];
            for (int j = 0; j < gameState[i].length; j++) {
                tmpGameState[i][j] = gameState[i][j];
            }
        }
        tmpGameState = movePiece(startRank, startFile, moveRank, moveFile,
                tmpGameState);
        int kingRank = -1;
        int kingFile = -1;
        String kingString;
        String opposingColor;
        if (isWhite) {
            kingString = "wK";
            opposingColor = "b";
        } else {
            kingString = "bK";
            opposingColor = "w";
        }
        for (int i = 0; i < tmpGameState.length; i++) {
            for (int j = 0; j < tmpGameState[i].length; j++) {
                if (tmpGameState[i][j].equals(kingString)) {
                    kingRank = i;
                    kingFile = j;
                }
            }
        }
        String opposingBishop = opposingColor + "B";
        String opposingQueen = opposingColor + "Q";
        String opposingRook = opposingColor + "R";
        String opposingPawn = opposingColor + "P";
        boolean upLeftRuledOut = false;
        boolean upRightRuledOut = false;
        boolean downLeftRuledOut = false;
        boolean downRightRuledOut = false;
        //check for queen/bishop/pawn diagonally
        for (int i = 1; i < tmpGameState.length; i++) {
            if ((kingRank - i) >= 0) {
                if ((kingFile - i) >= 0 && !upLeftRuledOut) {
                    String positionString = tmpGameState[kingRank - i]
                        [kingFile - i];
                    if (i == 1) {
                        if (positionString.equals(opposingPawn)) {
                            return true;
                        }
                    }
                    if (positionString.equals(opposingBishop)
                            || positionString.equals(opposingQueen)) {
                        return true;
                    }
                    if (!positionString.equals("  ")) {
                        upLeftRuledOut = true;
                    }
                }
                if ((kingFile + i) <= 7 && !upRightRuledOut) {
                    String positionString = tmpGameState[kingRank - i]
                        [kingFile + i];
                    if (i == 1) {
                        if (positionString.equals(opposingPawn)) {
                            return true;
                        }
                    }
                    if (positionString.equals(opposingBishop)
                            || positionString.equals(opposingQueen)) {
                        return true;
                    }
                    if (!positionString.equals("  ")) {
                        upRightRuledOut = true;
                    }
                }
            }
            if ((kingRank + i) <= 7) {
                if ((kingFile - i) >= 0 && !downLeftRuledOut) {
                    String positionString = tmpGameState[kingRank + i]
                        [kingFile - i];
                    if (i == 1) {
                        if (positionString.equals(opposingPawn)) {
                            return true;
                        }
                    }
                    if (positionString.equals(opposingBishop)
                            || positionString.equals(opposingQueen)) {
                        return true;
                    }
                    if (!positionString.equals("  ")) {
                        downLeftRuledOut = true;
                    }
                }
                if ((kingFile + i) <= 7 && !downRightRuledOut) {
                    String positionString = tmpGameState[kingRank + i]
                        [kingFile + i];
                    if (i == 1) {
                        if (positionString.equals(opposingPawn)) {
                            return true;
                        }
                    }
                    if (positionString.equals(opposingBishop)
                            || positionString.equals(opposingQueen)) {
                        return true;
                    }
                    if (!positionString.equals("  ")) {
                        downRightRuledOut = true;
                    }
                }
            }
        }
        //check for rook/queen checks
        for (int i = kingRank + 1; i < tmpGameState.length; i++) {
            if (tmpGameState[i][kingFile].equals(opposingRook)
                    || tmpGameState[i][kingFile].equals(opposingQueen)) {
                return true;
            }
            if (!tmpGameState[i][kingFile].equals("  ")) {
                break;
            }
        }
        for (int i = kingRank - 1; i >= 0; i--) {
            if (tmpGameState[i][kingFile].equals(opposingRook)
                    || tmpGameState[i][kingFile].equals(opposingQueen)) {
                return true;
            }
            if (!tmpGameState[i][kingFile].equals("  ")) {
                break;
            }
        }
        for (int i = kingFile - 1; i >= 0; i--) {
            if (tmpGameState[kingRank][i].equals(opposingRook)
                    || tmpGameState[kingRank][i].equals(opposingQueen)) {
                return true;
            }
            if (!tmpGameState[kingRank][i].equals("  ")) {
                break;
            }
        }
        for (int i = kingFile + 1; i < tmpGameState[kingRank].length; i++) {
            if (tmpGameState[kingRank][i].equals(opposingRook)
                    || tmpGameState[kingRank][i].equals(opposingQueen)) {
                return true;
            }
            if (!tmpGameState[kingRank][i].equals("  ")) {
                break;
            }
        }
        return false;
    }
}

