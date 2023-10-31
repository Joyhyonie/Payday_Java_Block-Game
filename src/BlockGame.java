import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class BlockGame {

    Scanner sc = null;
    static final int SIZE = 5;
    static char[][] board = new char[SIZE][SIZE];
    static int[][] blocks = {{1, 1, 1}, {0, 1, 1, 1}, {1, 0, 1, 1}, {1, 1, 1, 0}, {1, 1, 0, 1}};
    static String nickname;

    public void startGame() {

        Starting:
        while(true) {

            // board 생성
            for(int i = 0; i < SIZE; i++) {
                for(int j = 0; j < SIZE; j++) board[i][j] = 'o';
            }

            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("============= BLOCK GAME ============");
            System.out.println("         \uD83E\uDD16 똑똑이 블럭 게임 \uD83E\uDD16         ");
            System.out.println("=====================================");
            System.out.println("[1] 선공");
            System.out.println("[2] 후공");
            System.out.println("[0] 끝내기");
            int turn;

            // InputMismatchException 예외처리
            while(true) {

                sc = new Scanner(System.in);

                try {
                    System.out.println("=====================================");
                    System.out.print("\uD83E\uDD16 번호 입력: ");
                    turn = sc.nextInt();
                    break;
                } catch (InputMismatchException e) {
                    System.out.println("\uD83E\uDD16 문자는 입력할 수 없습니다. 다시 입력해주세요.");
                }

            }

            System.out.println("=====================================");

            if(turn == 0) {
                System.out.println("\uD83E\uDD16 게임을 종료합니다.");
                sc.close();
                break Starting;
            }

            System.out.print("\uD83E\uDD16 닉네임 입력: ");
            nickname = sc.next();

            switch (turn) {
                case 1: firstTurn(); break;
                case 2: runGame(); break;
                default: System.out.println("\uD83E\uDD16 번호가 올바르지 않습니다. 처음부터 다시 입력해주세요."); break;
            }

        }

    }

    /* 첫 선공, 랜덤한 블럭&좌표 지정 후 board를 출력하기 위한 메소드 */
    public void firstTurn() {

        int blockNum = (int)(Math.random() * 5) + 1;
        int row, col;

        if(blockNum == 1) {
            row = (int)(Math.random() * 4);
            col = (int)(Math.random() * 2);
        } else {
            row = (int)(Math.random() * 3);
            col = (int)(Math.random() * 3);
        }

        System.out.println("=====================================");
        System.out.println("\uD83E\uDD16 " + nickname + "님이 " + blockNum + "번 블럭을 " + row + "행, " + col + "열에 두었습니다.");

        setBoard(blockNum, row, col, board);
        printBoard();
        runGame();
    }

    /* 게임 진행 시, 지속적으로 반복되는 메소드 */
    public void runGame() {

        Running:
        while(true) {

            int number;
            System.out.println("=====================================");
            System.out.println("\uD83E\uDD16 상대방의 블럭 및 좌표(행/열)를 순서대로 숫자만 입력해주세요.");
            System.out.println("(ex: 3번 블럭을 2행, 1열에 둠 ▶ 321)");

            // InputMismatchException 예외처리 및 길이 검사
            while(true) {

                sc = new Scanner(System.in);

                try {
                    System.out.print(": ");
                    number = sc.nextInt();

                    // 유효성 검사 (길이)
                    if((int)(Math.log10(number)+1) != 3) {
                        boolean check = true;
                        while(check == true) {
                            System.out.println("\uD83E\uDD16 번호가 올바르지 않습니다. 다시 입력해주세요.");
                            System.out.print(": ");
                            number = sc.nextInt();
                            check = (int)(Math.log10(number)+1) != 3;
                        }
                    }
                    break;
                } catch (InputMismatchException e) {
                    System.out.println("\uD83E\uDD16 문자는 입력할 수 없습니다. 다시 입력해주세요.");
                }

            }

            /* 상대방 블럭 체크 */
            // 입력받은 number의 각 index를 구하기 위한 arr
            int[] arrNum = Stream.of(String.valueOf(number).split("")).mapToInt(Integer::parseInt).toArray();

            // 유효 블럭 확인
            boolean isPossible = checkBoard(arrNum[0], arrNum[1], arrNum[2]);

            if(!isPossible) {
                System.out.println("\uD83E\uDD16 상대방의 블럭을 해당 좌표에 둘 수 없습니다. " + nickname + "님의 승리! \uD83C\uDF89");
                System.out.println("============= GAME OVER ============");
                break Running;
            } else {
                System.out.println("=====================================");
                System.out.println("\uD83E\uDD16 상대방이 " + arrNum[0] + "번 블럭을 " + arrNum[1] + "행, " + arrNum[2] + "열에 두었고,");
                setBoard(arrNum[0], arrNum[1], arrNum[2], board);
            }

            /* 나의 최선 블럭 및 좌표 찾기 */
            int[] result = findTheBestWay();

            /* 현재 board에 나와 상대방의 블럭 및 좌표를 추가한 board 출력 */
            if(result[0] == 0 && result[1] == 0 && result[2] == 0) {
                System.out.println("\uD83E\uDD16 더이상 " + nickname + "님의 블럭을 둘 공간이 없습니다. 상대방의 승리.. \uD83D\uDE2D");
                System.out.println("============= GAME OVER ============");
                break Running;
            } else {
                System.out.println("\uD83E\uDD16 " + nickname + "님은 " + result[0] + "번 블럭을 " + result[1] + "행, " + result[2] + "열에 두었습니다.");
                setBoard(result[0], result[1], result[2], board);
                printBoard();

                // setBoard 후, 상대방이 놓을 곳이 없다면, 나의 승리
                result = findTheBestWay();
                if(result[0] == 0 && result[1] == 0 && result[2] == 0) {
                    System.out.println("=====================================");
                    System.out.println("\uD83E\uDD16 더이상 상대방의 블럭을 둘 공간이 없습니다. " + nickname + "님의 승리! \uD83C\uDF89");
                    System.out.println("============= GAME OVER ============");
                    break Running;
                }
            }

        }

    }

    /* board에 들어갈 수 있는 유효한 block인지 확인하기 위한 메소드 */
    public boolean checkBoard(int blockNum, int row, int col) {

        int count = 0;
        int blockIndex = 0;

        // index를 초과한 좌표가 입력될 경우 발생될 ArrayIndexOutOfBoundsException 예외처리
        try {
            if(blockNum == 1) {
                for(int i = 0; i < 5; i++) {
                    if(i == row) {
                        for(int j = col; j < col+3; j++) {
                            if(board[i][j] == 'x') count++;
                        }
                    }
                }
            } else {
                for(int i = 0; i < 4; i++) {
                    if(i == row || i == row+1) {
                        for(int j = col; j < col+2; j++) {
                            if(board[i][j] == 'x' && blocks[blockNum-1][blockIndex] == 1) count++;
                            blockIndex++;
                        }
                    }
                }
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }

        return count > 0 ? false : true;
    }

    /* 최선의 블럭 및 좌표를 찾아내기 위한 메소드 */
    public int[] findTheBestWay() {

        // 현재 가능한 모든 경우의 수 List
        List<List<int[]>> allXys = new ArrayList<>();

        // 각 블럭마다 반복문을 돌려서 경우의 수(List.size())를 구해야 하는 메소드를 따로 선언하기 (tempBoard에 넣고나서의 경우의 수도 구해야하므로)
        for(int blockNum = 1; blockNum <= 5; blockNum++) {
            List<int[]> xys = collectXys(blockNum, board);
            allXys.add(xys);
        }

        // 블럭(allXys의 순서)당 좌표(xys의 요소) 하나하나, board에 넣고(tempBoard) 거기서 collectXys()를 호출하여 tempBoard의 경우의 수 추출 후 count해서 반환(casesCount)
        int minCasesCount = 79;
        int[] way = new int[3];

        for(int i = 0; i < allXys.size(); i++) {
            List<int[]> xys = allXys.get(i);
            for(int j = 0; j < xys.size(); j++) {
                List<Integer> casesCountList = predictNext(i + 1, xys.get(j), board);
                // System.out.println("현재 predictNext()를 타고 다녀온 블럭/좌표/경우의 수 => " + (i+1) + "/" + xys.get(j)[0] + xys.get(j)[1] + "/" + casesCount);
                System.out.println("현재 casesCountList.size() ???? " + casesCountList.size() + "-------------------------------------");

                // List.size()가 홀수일 때 (이기는 방법)
                if (casesCountList.size() % 2 == 1) {
                    way[0] = i + 1;
                    way[1] = xys.get(j)[0];
                    way[2] = xys.get(j)[1];
                  // 홀수인 List.size()가 없다면?
                } else {
                    System.out.println("헤헤 진건가");
                }

            }
        }

        // way가 없을 경우, [0, 0, 0] 반환하여 패배
        return way;

    }

    /* tempBoard를 활용하여 다음의 경우의 수를 예측하기 위한 메소드 */
    public List<Integer> predictNext(int blockNum, int[] xy, char[][] board) {
        // ex: 1번 블럭을 board의 2,2위치에 놓을 때의 casesCount를 계산할 것.....

        List<Integer> casesCountList = new ArrayList<>();
        // return 해야하는 형태: {10, 5, 2, 0}
        // 조건을 List.get(i)의 길이가 홀or짝인 것으로 판단해도 좋을듯? 0이면 해당 i는 종료되므로

        int casesCount = 0;

        char[][] tempBoard = new char[SIZE][SIZE];
        // 2차원 배열 깊은 복사의 경우, for loop안에서 clone() 해야함
        for(int i = 0; i < board.length; i++) {
            tempBoard[i] = board[i].clone();
        }

        // setBoard()를 통해 tempBoard 완성
        setBoard(blockNum, xy[0], xy[1], tempBoard);

        // tempAllXys: tempBoard에서 가능한 '블럭별' 모든 경우의 수 List
        // xys: tempBoard에 tempBN을 넣을 수 있는 경우의 수
        List<List<int[]>> tempAllXys = new ArrayList<>();

        // collectXys()를 통해 tempBoard의 경우의 수 구하기
        for(int tempBlockNum = 1; tempBlockNum <= 5; tempBlockNum++) {
            List<int[]> xys = collectXys(tempBlockNum, tempBoard);
            tempAllXys.add(xys);
            casesCount += xys.size();
        }

        casesCountList.add(casesCount);

        // case가 더 있다면, predictNext() 재귀호출
        if(casesCount != 0) {

            for(int i = 0; i < tempAllXys.size(); i++) {
                List<int[]> xys = tempAllXys.get(i);
                for(int j = 0; j < xys.size(); j++) {
                    List<Integer> tempCasesCountList = predictNext(i + 1, xys.get(j), tempBoard);
                    System.out.println(j + "번째 predictNext() 재귀 호출 => " + (i+1) + "/" + xys.get(j)[0] + xys.get(j)[1] + "/" + casesCount);
                }
            }
        }

        return casesCountList;

    }

    /* 각 block별, 현재 board에 둘 수 있는 좌표(경우의 수)들을 List로 반환하기 위한 메소드 */
    public List<int[]> collectXys(int blockNum, char[][] board) {

        List<int[]> xys = new ArrayList<>();

        if(blockNum == 1) {
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 3; j++) {
                    if(board[i][j] == 'o' && board[i][j+1] == 'o' && board[i][j+2] == 'o') { int[] xy = {i, j}; xys.add(xy); }
                }
            }
        } else {
            for(int i = 0; i < 4; i++) {
                for(int j = 0; j < 4; j++) {
                    if(blockNum == 2) {
                        if(board[i][j+1] == 'o' && board[i+1][j] == 'o' && board[i+1][j+1] == 'o') { int[] xy = {i, j}; xys.add(xy); }
                    } else if(board[i][j] == 'o') {
                        switch (blockNum) {
                            case 3: if(board[i+1][j] == 'o' && board[i+1][j+1] == 'o') { int[] xy = {i, j}; xys.add(xy); } break;
                            case 4: if(board[i][j+1] == 'o' && board[i+1][j] == 'o') { int[] xy = {i, j}; xys.add(xy); } break;
                            case 5: if(board[i][j+1] == 'o' && board[i+1][j+1] == 'o') { int[] xy = {i, j}; xys.add(xy); } break;
                        }
                    }
                }
            }
        }

        return xys;

    }

    /* board에 block을 두기 위한 메소드 */
    public void setBoard(int blockNum, int row, int col, char[][] board) {

        if(blockNum == 1) {
            for(int i = 0; i < 5; i++) {
                if(i == row) {
                    for(int j = col; j < col+3; j++) {
                        board[i][j] = 'x';
                    } break;
                }
            }
        } else {
            int blockIndex = 0;
            for(int i = 0; i < 5; i++) {
                if(i == row || i == row+1) {
                    for(int j = col; j < col+2; j++) {
                        if(blocks[blockNum-1][blockIndex++] == 1) {
                            board[i][j] = 'x';
                        }
                    }
                }
            }
        }

    }

    /* board를 출력하기 위한 메소드 */
    public void printBoard() {

        System.out.println("=====================================");
        System.out.println("    0 1 2 3 4");
        for(int i = 0; i < 5; i++) {
            System.out.print(i + " [ ");
            for(int j = 0; j < 5; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println("]");
        }

    }

}
