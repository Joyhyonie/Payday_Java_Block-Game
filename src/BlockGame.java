import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class BlockGame {

    Scanner sc = new Scanner(System.in);
    static char[][] board = new char[5][5];
    static int[][] blocks = {{1, 1, 1}, {0, 1, 1, 1}, {1, 0, 1, 1}, {1, 1, 1, 0}, {1, 1, 0, 1}};
    static String nickname;

    public void startGame() {

        // InputMismatchException 예외처리하기 (2곳)

        Starting:
        while(true) {

            /* board 생성 */
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 5; j++) board[i][j] = 'o';
            }

            System.out.println();
            System.out.println("============= BLOCK GAME ============");
            System.out.println("         \uD83E\uDD16 똑똑이 블럭 게임 \uD83E\uDD16         ");
            System.out.println("=====================================");
            System.out.print("\uD83E\uDD16 닉네임 입력: ");
            nickname = sc.next();
            System.out.println("=====================================");
            // 1. 선공 or 후공 선택
            System.out.println("[1] 선공");
            System.out.println("[2] 후공");
            System.out.println("[0] 끝내기");
            System.out.println("=====================================");
            System.out.print("\uD83E\uDD16 번호 입력: ");
            int turn = sc.nextInt();

            switch (turn) {
                case 1: firstTurn(); break;
                case 2: runGame(); break;
                case 0: System.out.println("\uD83E\uDD16 게임을 종료합니다."); sc.close(); break Starting;
                default: System.out.println("\uD83E\uDD16 입력이 올바르지 않습니다. 처음부터 다시 입력해주세요"); break;
            }
        }

    }

    /* 첫 선공, 랜덤한 블럭&좌표 지정 후 board 출력 */
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

        /* set block on the board */
        setBoard(blockNum, row, col);
        /* print the board */
        printBoard();
        /* 본격 게임 실행 */
        runGame();
    }

    public void runGame() {

        Running:
        while(true) {

            int number;

            System.out.println("=====================================");
            System.out.println("\uD83E\uDD16 상대방의 블럭 및 좌표(행/열)를 순서대로 숫자만 입력해주세요.");
            System.out.println("(ex: 3번 블럭을 2행, 1열에 둠 => 321)");
            System.out.print(": ");
            number = sc.nextInt();

            if((int)(Math.log10(number)+1) != 3) {
                boolean check = true;
                while(check == true) {
                    System.out.println("\uD83E\uDD16 올바르지 않습니다. 다시 입력해주세요.");
                    System.out.print(": ");
                    number = sc.nextInt();
                    check = (int)(Math.log10(number)+1) != 3;
                }
            }

            /* 상대방 블럭 체크 */
            // 입력받은 number의 각 index를 구하기 위한 arr
            int[] arrNum = Stream.of(String.valueOf(number).split("")).mapToInt(Integer::parseInt).toArray();
            // 단순 유효 블럭 체크
            boolean isPossible = checkBoard(arrNum[0], arrNum[1], arrNum[2]);

            if(!isPossible) {
                System.out.println("\uD83E\uDD16 상대방의 블럭을 해당 좌표에 둘 수 없습니다." + nickname + "님의 승리! \uD83C\uDF89");
                System.out.println("============= GAME OVER ============");
                System.out.println();
                System.out.println();
                break Running;
            } else {
                System.out.println("=====================================");
                System.out.println("\uD83E\uDD16 상대방이 " + arrNum[0] + "번 블럭을 " + arrNum[1] + "행, " + arrNum[2] + "열에 두었고,");
                setBoard(arrNum[0], arrNum[1], arrNum[2]);
            }

            /* 나의 최적화된 블럭/좌표 찾기 */
            int[] result = findTheBestWay();

            /* 현재 board + 상대방 좌표 + 알고리즘 좌표를 추가한 board 출력 */
            if(result[0] == 0 && result[1] == 0 && result[2] == 0) {
                System.out.println("\uD83E\uDD16 더이상 " + nickname + "님의 블럭을 둘 공간이 없습니다. 상대방의 승리.. \uD83D\uDE2D");
                System.out.println("============= GAME OVER ============");
                System.out.println();
                System.out.println();
                break Running;
            } else {
                System.out.println("\uD83E\uDD16 " + nickname + "님은 " + result[0] + "번 블럭을 " + result[1] + "행, " + result[2] + "열에 두었습니다.");
                setBoard(result[0], result[1], result[2]);
                printBoard();
            }

        }

    }

    /* board에 block이 들어갈 수 있는지 check하기 위한 메소드 */
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

    /* 최선의 블럭/좌표를 찾아내기 위한 메소드 */
    public int[] findTheBestWay() {

        // 현재 가능한 모든 경우의 수 List
        List<List<int[]>> allXys = new ArrayList<>();

        // 각 블럭마다 반복문을 돌려서 경우의 수(List.size())를 구해야 하는 메소드를 따로 선언하기 (tempBoard에 넣고나서의 경우의 수도 구해야하므로)
        for(int blockNum = 1; blockNum <= 5; blockNum++) {
            List<int[]> xys = collectXys(blockNum, board);
            allXys.add(xys);
        }

        // 블럭(allXys의 순서)당 좌표(xys의 요소) 하나하나, board에 넣고(tempBoard) 거기서 collectXys()를 호출하여 tempBoard의 경우의 수 추출 후 count해서 반환(casesCount)
        int minCasesCount = 20;
        int[] way = new int[3];

        for(int i = 0; i < allXys.size(); i++) {
            List<int[]> xys = allXys.get(i);
//            System.out.println("지금 i (i는 4까지 증가해야함) => " + i + " --------------------------------------------------------");
            for(int j = 0; j < xys.size(); j++) {
//                System.out.println("지금 j (j는 배열의 갯수만큼 증가해야함) => " + j);
                int casesCount = predictNext(i + 1, xys.get(j));
//                System.out.println("현재 predictNext()를 타고 다녀온 블럭/좌표/경우의 수 => " + (i+1) + "/" + xys.get(j)[0] + xys.get(j)[1] + "/" + casesCount);
                // casesCount가 이전보다 작다면, minCasesCount에 저장하고, minCasesCount가 저장된 시점의 index와 xy 또한 함께 변수에 저장
                if (casesCount < minCasesCount) {
                    minCasesCount = casesCount;
                    way[0] = i + 1;
                    way[1] = xys.get(j)[0];
                    way[2] = xys.get(j)[1];
                }
//                System.out.println("현재 가장 작은 case 개수 : " + minCasesCount);
//                System.out.println("            그때의 좌표 : " + way[0] + way[1] + way[2]);
            }
        }

        // [테스트] 좌표 출력용
//        int blockNum = 1;
//        for(List<int[]> o : allXys) {
//            System.out.println();
//            System.out.println("[TEST] 현재 가능 좌표 출력 -------------------------------" + blockNum + "번 블럭");
//            for(int[] oo : o) {
//                for(int ooo : oo) {
//                    System.out.print(ooo);
//                }
//                System.out.print(", ");
//            } blockNum++;
//
//        }
//        System.out.println();
//        System.out.println();

        // way가 없을 경우, [0, 0, 0] 반환하여 패배
        return way;

    }

    public int predictNext(int blockNum, int[] xy) {

        char[][] tempBoard = new char[5][5];
        // 2차원 배열 깊은 복사의 경우, for loop안에서 clone() 해야함
        for(int i = 0; i < board.length; i++) {
            tempBoard[i] = board[i].clone();
        }

        // [임시] tempBoard용 setBoard()
        if(blockNum == 1) {
            for(int i = 0; i < 5; i++) {
                if(i == xy[0]) {
                    for(int j = xy[1]; j < xy[1]+3; j++) {
                        tempBoard[i][j] = 'x';
                    } break;
                }
            }
        } else {
            int blockIndex = 0;
            for(int i = 0; i < 5; i++) {
                if(i == xy[0] || i == xy[0]+1) {
                    for(int j = xy[1]; j < xy[1]+2; j++) {
                        if(blocks[blockNum-1][blockIndex++] == 1) {
                            tempBoard[i][j] = 'x';
                        }
                    }
                }
            }
        }

        // [임시] tempBoard 출력용
        System.out.println("============ tempBoard ==============");
        System.out.println("    0 1 2 3 4");
        for(int i = 0; i < 5; i++) {
            System.out.print(i + " [ ");
            for(int j = 0; j < 5; j++) {
                System.out.print(tempBoard[i][j] + " ");
            }
            System.out.println("]");
        }

        List<int[]> xys = collectXys(blockNum, tempBoard);

        return xys.size(); // 경우의 수

    }

    /* 각 block마다 현재 board에 가능한 좌표(경우의 수)들을 List로 반환하는 메소드 */
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

    /* board에 block을 단순 set하기위한 메소드 */
    public void setBoard(int blockNum, int row, int col) {

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
            for(int i = 0; i < 5; i++) { // 5여야함
                if(i == row || i == row+1) {
                    for(int j = col; j < col+2; j++) {
                        if(blocks[blockNum-1][blockIndex++] == 1) { // 현재 여기서 Array exception 발생중
                            board[i][j] = 'x';
                        }
                    }
                }
            }
        }

    }

    /* board를 출력하기위한 메소드 */
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
