import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class BlockGame {

    Scanner sc = new Scanner(System.in);
    static char[][] board = new char[5][5];
    static int[][] blocks = {{1, 1, 1}, {0, 1, 1, 1}, {1, 0, 1, 1}, {1, 1, 1, 0}, {1, 1, 0, 1}};
    static String nickname;

    public void startGame() {

        Starting:
        while(true) {

            /* board 생성 */
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 5; j++) board[i][j] = 'o';
            }

            System.out.println();
            System.out.println("============= Block Game ============");
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

        int number;

        Running:
        while(true) {

            System.out.println("=====================================");
            System.out.println("\uD83E\uDD16 상대방의 블럭 및 좌표(행/열)를 순서대로 숫자만 입력해주세요.");
            System.out.println("(ex: 3번 블럭을 2행, 1열에 둠 => 321)");
            System.out.print(": ");
            number = sc.nextInt();

            if((int)(Math.log10(number)+1) != 3) { // 유효성 추가 예정: number가 int인지, 놓을 수 있는지
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
                System.out.println("\uD83E\uDD16 상대방의 블록을 해당 좌표에 둘 수 없습니다. Game Over. You're a winner! \uD83C\uDF89");
                break Running; // +break Starting;
            } else {
                System.out.println("=====================================");
                System.out.println("\uD83E\uDD16 상대방이 " + arrNum[0] + "번 블럭을 " + arrNum[1] + "행, " + arrNum[2] + "열에 두었고,");
                setBoard(arrNum[0], arrNum[1], arrNum[2]);
            }

            /* 나의 최적화된 블럭/좌표 찾기 */
            int[] result = findTheBestWay();

            /* 현재 board + 상대방 좌표 + 알고리즘 좌표를 추가한 board 출력 */
            System.out.println("\uD83E\uDD16 " + nickname + "님은 " + result[0] + "번 블럭을 " + result[1] + "행, " + result[2] + "열에 두었습니다.");
            printBoard();

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

        int[] way = new int[3];
        char[][] tempBoard = new char[5][5];
        List<List<int[]>> allXys = new ArrayList<>();

        // 각 블럭마다 반복문을 돌려서 경우의 수(List.size())를 구해야 하는 메소드를 따로 선언하기 (tempBoard에 넣고나서의 경우의 수도 구해야하므로)
        for(int blockNum = 1; blockNum <= 5; blockNum++) {
            List<int[]> xys = collectXys(blockNum, board);
            allXys.add(xys);
        }

        // 테스트
//        for(List<int[]> o : allXys) {
//            System.out.print("[테스트] 각 좌표 출력");
//            for(int[] oo : o) {
//                for(int ooo : oo) {
//                    System.out.print(ooo);
//                }
//                System.out.println();
//            }
//        }


//        setBoard();

        return way;

    }

    /* 각 block마다 현재 board에 가능한 좌표들을 List로 반환하는 메소드 */
    public List<int[]> collectXys(int blockNum, char[][] board) {

        List<int[]> xys = new ArrayList<>();

        if(blockNum == 1) {
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 3; j++) {
                    // 테스트
//                    int[] xy = {3, 5};
//                    xys.add(xy);
                }
            }
        } else {
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 4; j++) {
                    // 테스트
//                    int[] xy = {5, 9};
//                    xys.add(xy);
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

    /* board를 출력하기위한 메소드 */
    public void printBoard() {

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
