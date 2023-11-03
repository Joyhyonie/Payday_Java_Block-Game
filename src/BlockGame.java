import com.sun.security.jgss.GSSUtil;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Stream;

public class BlockGame {

    Scanner sc = null;
    static final int SIZE = 5;
    static char[][] board = new char[SIZE][SIZE];
    static int[][] blocks = {{1, 1, 1}, {0, 1, 1, 1}, {1, 0, 1, 1}, {1, 1, 1, 0}, {1, 1, 0, 1}};
    static String nickname;
    List<List<int[]>> winningRoutes = new ArrayList<>(); // 각 시점, 이길 수 있는 루트를 저장하는 리스트


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

                    int[] firstArrNum = Stream.of(String.valueOf(number).split("")).mapToInt(Integer::parseInt).toArray();

                    // 유효성 검사 (길이 및 좌표)
                    if((int)(Math.log10(number)+1) != 3 || firstArrNum[0] > 5 || firstArrNum[1] > 4 || firstArrNum[2] > 4) {
                        while(true) {
                            System.out.println("\uD83E\uDD16 번호가 올바르지 않습니다. 다시 입력해주세요.");
                            System.out.print(": ");
                            number = sc.nextInt();
                            int[] nextArrNum = Stream.of(String.valueOf(number).split("")).mapToInt(Integer::parseInt).toArray();
                            // 아래의 조건을 충족하는 경우에만, break;
                            if((int)Math.log10(number)+1 == 3 && nextArrNum[0] <= 5 && nextArrNum[1] <= 4 && nextArrNum[2] <= 4) break;
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
                List<List<int[]>> xysList = new ArrayList<>();

                for(int blockNum = 1; blockNum <= 5; blockNum++) {
                    List<int[]> xys = collectXys(blockNum, board);
                    if(!xys.isEmpty()) xysList.add(xys);
                }

                if(xysList.isEmpty()) {
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

        List<int[]> xysList = new ArrayList<>(); // 현재 가능한 경우의 수 List
        // collectXys(): 경우의 수(좌표) 구하기
        for(int blockNum = 1; blockNum <= 5; blockNum++) {
            List<int[]> xys = collectXys(blockNum, board);
            xysList.addAll(xys);
        }

        // winningRoutes에 앞으로 이길 수 있는 모든 Route 저장하는 callRecursive() 호출
        callRecursive(xysList, board);

        // map으로 가장 많이 등장한 route.get(0) 찾기
        Map<List<int[]>, Integer> routeCount = new HashMap<>();
        int maxCount = 0;
        List<int[]> mostFrequentRoute = null;

        for(List<int[]> route : winningRoutes) {
            List<int[]> key = List.of(route.get(0));
            int count = routeCount.getOrDefault(key, 0) + 1;
            routeCount.put(key, count);

            if (count > maxCount) {
                maxCount = count;
                mostFrequentRoute = key;
            }
        }

        // way가 없을 경우, [0, 0, 0] 반환하여 패배
        int[] way;
        // mostFrequentRoute가 null이라는 것은, 이길 수 있는 경우의 수가 더이상 존재하지않음을 의미. 하지만 마지막까지 수를 둬야하므로 현재 둘 수 있는 좌표 찾기
        if(mostFrequentRoute == null) {
            List<List<int[]>> losingXysList = new ArrayList<>();
            for(int blockNum = 1; blockNum <= 5; blockNum++) {
                List<int[]> losingXys = collectXys(blockNum, board);
                if(!losingXys.isEmpty()) losingXysList.add(losingXys); // losingXys.size()가 0인 경우에도 losingXysList에 담기므로, 조건문 설정
            }
            if(!losingXysList.isEmpty()) {
                way = new int[]{losingXysList.get(0).get(0)[0], losingXysList.get(0).get(0)[1], losingXysList.get(0).get(0)[2]};
            } else {
                way = new int[]{0, 0, 0};
            }
        } else {
            way = mostFrequentRoute.get(0);
        }

        return way;

    }
    
    /* findTheBestWay()에서 직접 호출하는 callRecursive() 메소드 */
    public void callRecursive(List<int[]> xysList, char[][] board) {
        winningRoutes.clear(); // 이전 시점의 winningRoutes 제거
        callRecursive(xysList, board, new ArrayList<>());
    }

    /* callRecursive() 내부에서 재귀 호출되는 메소드 */
    public void callRecursive (List<int[]> xysList, char[][] board, List<int[]> currentRoute) {

        for(int[] xy : xysList) {

            char[][] tempBoard = copyBoard(board);
            setBoard(xy[0], xy[1], xy[2], tempBoard);
            // xysList: 현재 둘 수 있는 요소들
            List<int[]> tempXysList = new ArrayList<>();

            for(int blockNum = 1; blockNum <= 5; blockNum++) {
                List<int[]> tempXys = collectXys(blockNum, tempBoard);
                tempXysList.addAll(tempXys); // 현재 xy의 가능한 모든 좌표
            }

            // tempXysList에 요소가 존재하지 않을 시(더이상 둘 곳이 없을 시), new Route를 List에 저장
            if(tempXysList.isEmpty()) {
                List<int[]> tempCurrentRoute = new ArrayList<>(currentRoute);
                tempCurrentRoute.add(xy);
                // Route의 size가 홀수인 것이 이기는 Route이므로, 검사 후 저장
                if(tempCurrentRoute.size() % 2 == 1) winningRoutes.add(tempCurrentRoute);
            } else {
                List<int[]> tempCurrentRoute = new ArrayList<>(currentRoute);
                tempCurrentRoute.add(xy);
                callRecursive(tempXysList, tempBoard, tempCurrentRoute); // 재귀 호출 시, 기존의 currentRoute와 함께 전달
            }
        }

    }


    /* 각 block별, 현재 board에 둘 수 있는 좌표(경우의 수)들을 List로 반환하기 위한 메소드 */
    public List<int[]> collectXys(int blockNum, char[][] board) {

        List<int[]> xys = new ArrayList<>();

        if(blockNum == 1) {
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 3; j++) {
                    if(board[i][j] == 'o' && board[i][j+1] == 'o' && board[i][j+2] == 'o') { int[] bxy = {blockNum, i, j}; xys.add(bxy); }
                }
            }
        } else {
            for(int i = 0; i < 4; i++) {
                for(int j = 0; j < 4; j++) {
                    if(blockNum == 2) {
                        if(board[i][j+1] == 'o' && board[i+1][j] == 'o' && board[i+1][j+1] == 'o') { int[] bxy = {blockNum, i, j}; xys.add(bxy); }
                    } else if(board[i][j] == 'o') {
                        switch (blockNum) {
                            case 3: if(board[i+1][j] == 'o' && board[i+1][j+1] == 'o') { int[] bxy = {blockNum, i, j}; xys.add(bxy); } break;
                            case 4: if(board[i][j+1] == 'o' && board[i+1][j] == 'o') { int[] bxy = {blockNum, i, j}; xys.add(bxy); } break;
                            case 5: if(board[i][j+1] == 'o' && board[i+1][j+1] == 'o') { int[] bxy = {blockNum, i, j}; xys.add(bxy); } break;
                        }
                    }
                }
            }
        }

        return xys;

    }

    public char[][] copyBoard(char[][] board) {

        char[][] tempBoard = new char[SIZE][SIZE];
        // 2차원 배열 깊은 복사의 경우, for loop안에서 clone() 해야함
        for(int i = 0; i < SIZE; i++) {
            tempBoard[i] = board[i].clone();
        }

        return tempBoard;

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