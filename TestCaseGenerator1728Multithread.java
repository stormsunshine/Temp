import java.io.*;
import java.util.*;

public class TestCaseGenerator1728Multithread {
	public static void main(String[] args) throws Exception {
		int length = 15;// 线程数的指数
		long total = 1L << length;// 线程数
		for (long i = 0; i < total; i++) {
			char[] first = generate(length, i);
			Runnable generator = new GeneratorMultithread(first);
			Thread thread = new Thread(generator);
			thread.start();
		}
	}

	private static char[] generate(int length, long index) {
		char[] first = new char[length];
		Arrays.fill(first, '.');
		for (int i = 0; i < length && index > 0; i++) {
			long remainder = index % 2;
			if (remainder == 1) {
				first[i] = '#';
			}
			index /= 2;
		}
		return first;
	}
}

class GeneratorMultithread implements Runnable {
	static final int SIDE = 8;
	static int total = SIDE * SIDE;
	static int remain = total - 3;
	static Solution1728 sol = new Solution1728();
	static int maxSteps = 0;
	static int threadId = 0;
	char[] floorWall;
	int startPos;

	public GeneratorMultithread(char[] first) {
		floorWall = new char[remain];
		Arrays.fill(floorWall, '.');
		startPos = first.length;
		System.arraycopy(first, 0, floorWall, 0, startPos);
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		List<String[]> grids = new ArrayList<String[]>();
		List<int[]> jumps = new ArrayList<int[]>();
		generate(grids, jumps, floorWall, startPos);
		String path = "1728 output " + SIDE + " " + threadId + ".txt";
		threadId++;
		if (grids.isEmpty()) {
			return;
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			int size = grids.size();
			for (int i = 0; i < size; i++) {
				String[] grid = grids.get(i);
				int[] jump = jumps.get(i);
				int catJump = jump[0], mouseJump = jump[1];
				String str = Arrays.toString(grid).replaceAll(" ", "");
				bw.write(str + "\r\n" + catJump + "\r\n" + mouseJump + "\r\n");
			}
			System.out.println(size);
			bw.write(size + "\r\n");
			long end = System.currentTimeMillis();
			System.out.println("Max possible steps with side " + SIDE + ": " + maxSteps);
			System.out.println("Time used: " + (end - start) / 1000 + " seconds");
			bw.write("Max possible steps with side " + SIDE + ": " + maxSteps + "\r\n");
			bw.write("Time used: " + (end - start) / 1000 + " seconds");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void generate(List<String[]> grids, List<int[]> jumps, char[] floorWall, int start) {
		backtrack(grids, jumps, floorWall, start);
	}

	private static void backtrack(List<String[]> grids, List<int[]> jumps, char[] floorWall, int index) {
		if (index == remain) {
			for (int foodRow = 0; foodRow < (SIDE + 1) / 2; foodRow++) {
				for (int foodColumn = foodRow; foodColumn < (SIDE + 1) / 2; foodColumn++) {
					for (int cat = 0; cat < total; cat++) {
						int catRow = cat / SIDE, catColumn = cat % SIDE;
						if (catRow == foodRow && catColumn == foodColumn) {
							continue;
						}
						for (int mouse = 0; mouse < total; mouse++) {
							int mouseRow = mouse / SIDE, mouseColumn = mouse % SIDE;
							if (mouseRow == foodRow && mouseColumn == foodColumn || mouseRow == catRow && mouseColumn == catColumn) {
								continue;
							}
							char[][] matrix = new char[SIDE][SIDE];
							matrix[foodRow][foodColumn] = 'F';
							matrix[catRow][catColumn] = 'C';
							matrix[mouseRow][mouseColumn] = 'M';
							for (int i = 0, j = 0; i < total; i++) {
								int row = i / SIDE, column = i % SIDE;
								if (matrix[row][column] == 'F' || matrix[row][column] == 'C' || matrix[row][column] == 'M') {
									continue;
								}
								matrix[row][column] = floorWall[j++];
							}
							String[] grid = matrix2Grid(matrix);
							test(grids, jumps, grid);
						}
					}
				}
			}
		} else {
			floorWall[index] = '#';
			backtrack(grids, jumps, floorWall, index + 1);
			floorWall[index] = '.';
			backtrack(grids, jumps, floorWall, index + 1);
		}
	}

	private static String[] matrix2Grid(char[][] matrix) {
		String[] grid = new String[SIDE];
		for (int i = 0; i < SIDE; i++) {
			StringBuffer sb = new StringBuffer();
			sb.append(new String(matrix[i]));
			grid[i] = sb.toString();
		}
		return grid;
	}

	private static void test(List<String[]> grids, List<int[]> jumps, String[] grid) {
		for (int catJump = 1; catJump < SIDE; catJump++) {
			for (int mouseJump = 1; mouseJump < SIDE; mouseJump++) {
				int[] ans = sol.canMouseWin(grid, catJump, mouseJump);
				int steps = ans[1];
				if (steps > maxSteps) {
					maxSteps = steps;
				}
				if (steps > Solution1728.MAX_MOVES) {
					System.out.println(Arrays.toString(ans));
					System.out.println(grid2Str(grid));
					System.out.println(catJump);
					System.out.println(mouseJump);
					grids.add(grid);
					jumps.add(new int[]{catJump, mouseJump});
				}
			}
		}
	}

	private static String grid2Str(String[] grid) {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		int rows = grid.length;
		for (int i = 0; i < rows; i++) {
			if (i > 0) {
				sb.append(',');
			}
			StringBuffer line = new StringBuffer();
			line.append('"');
			line.append(grid[i]);
			line.append('"');
			sb.append(line);
		}
		sb.append(']');
		return sb.toString();
	}
}

class Solution1728 {
    static final int MOUSE_TURN = 0, CAT_TURN = 1;
    static final int UNKNOWN = 0, MOUSE_WIN = 1, CAT_WIN = 2;
    static final int MAX_MOVES = 73;
    int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    int rows, cols;
    String[] grid;
    int catJump, mouseJump;
    int food;
    int[][][] degrees;
    int[][][][] results;

    public int[] canMouseWin(String[] grid, int catJump, int mouseJump) {
        this.rows = grid.length;
        this.cols = grid[0].length();
        this.grid = grid;
        this.catJump = catJump;
        this.mouseJump = mouseJump;
        int startMouse = -1, startCat = -1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = grid[i].charAt(j);
                if (c == 'M') {
                    startMouse = getPos(i, j);
                } else if (c == 'C') {
                    startCat = getPos(i, j);
                } else if (c == 'F') {
                    food = getPos(i, j);
                }
            }
        }
        int total = rows * cols;
        degrees = new int[total][total][2];
        results = new int[total][total][2][2];
        Queue<int[]> queue = new ArrayDeque<int[]>();
        for (int mouse = 0; mouse < total; mouse++) {
            int mouseRow = mouse / cols, mouseCol = mouse % cols;
            if (grid[mouseRow].charAt(mouseCol) == '#') {
                continue;
            }
            for (int cat = 0; cat < total; cat++) {
                int catRow = cat / cols, catCol = cat % cols;
                if (grid[catRow].charAt(catCol) == '#') {
                    continue;
                }
                degrees[mouse][cat][MOUSE_TURN]++;
                degrees[mouse][cat][CAT_TURN]++;
                for (int[] dir : dirs) {
                    for (int row = mouseRow + dir[0], col = mouseCol + dir[1], jump = 1; row >= 0 && row < rows && col >= 0 && col < cols && grid[row].charAt(col) != '#' && jump <= mouseJump; row += dir[0], col += dir[1], jump++) {
                        int nextMouse = getPos(row, col), nextCat = getPos(catRow, catCol);
                        degrees[nextMouse][nextCat][MOUSE_TURN]++;
                    }
                    for (int row = catRow + dir[0], col = catCol + dir[1], jump = 1; row >= 0 && row < rows && col >= 0 && col < cols && grid[row].charAt(col) != '#' && jump <= catJump; row += dir[0], col += dir[1], jump++) {
                        int nextMouse = getPos(mouseRow, mouseCol), nextCat = getPos(row, col);
                        degrees[nextMouse][nextCat][CAT_TURN]++;
                    }
                }
            }
        }
        for (int pos = 0; pos < total; pos++) {
            int row = pos / cols, col = pos % cols;
            if (grid[row].charAt(col) == '#') {
                continue;
            }
            results[pos][pos][MOUSE_TURN][0] = CAT_WIN;
            results[pos][pos][MOUSE_TURN][1] = 0;
            results[pos][pos][CAT_TURN][0] = CAT_WIN;
            results[pos][pos][CAT_TURN][1] = 0;
            queue.offer(new int[]{pos, pos, MOUSE_TURN});
            queue.offer(new int[]{pos, pos, CAT_TURN});
        }
        for (int mouse = 0; mouse < total; mouse++) {
            int mouseRow = mouse / cols, mouseCol = mouse % cols;
            if (grid[mouseRow].charAt(mouseCol) == '#' || mouse == food) {
                continue;
            }
            results[mouse][food][MOUSE_TURN][0] = CAT_WIN;
            results[mouse][food][MOUSE_TURN][1] = 0;
            results[mouse][food][CAT_TURN][0] = CAT_WIN;
            results[mouse][food][CAT_TURN][1] = 0;
            queue.offer(new int[]{mouse, food, MOUSE_TURN});
            queue.offer(new int[]{mouse, food, CAT_TURN});
        }
        for (int cat = 0; cat < total; cat++) {
            int catRow = cat / cols, catCol = cat % cols;
            if (grid[catRow].charAt(catCol) == '#' || cat == food) {
                continue;
            }
            results[food][cat][MOUSE_TURN][0] = MOUSE_WIN;
            results[food][cat][MOUSE_TURN][1] = 0;
            results[food][cat][CAT_TURN][0] = MOUSE_WIN;
            results[food][cat][CAT_TURN][1] = 0;
            queue.offer(new int[]{food, cat, MOUSE_TURN});
            queue.offer(new int[]{food, cat, CAT_TURN});
        }
        while (!queue.isEmpty()) {
            int[] state = queue.poll();
            int mouse = state[0], cat = state[1], turn = state[2];
            int result = results[mouse][cat][turn][0];
            int moves = results[mouse][cat][turn][1];
            List<int[]> prevStates = getPrevStates(mouse, cat, turn);
            for (int[] prevState : prevStates) {
                int prevMouse = prevState[0], prevCat = prevState[1], prevTurn = prevState[2];
                if (results[prevMouse][prevCat][prevTurn][0] == UNKNOWN) {
                    boolean canWin = (result == MOUSE_WIN && prevTurn == MOUSE_TURN) || (result == CAT_WIN && prevTurn == CAT_TURN);
                    if (canWin) {
                        results[prevMouse][prevCat][prevTurn][0] = result;
                        results[prevMouse][prevCat][prevTurn][1] = moves + 1;
                        queue.offer(new int[]{prevMouse, prevCat, prevTurn});
                    } else {
                        degrees[prevMouse][prevCat][prevTurn]--;
                        if (degrees[prevMouse][prevCat][prevTurn] == 0) {
                            int loseResult = prevTurn == MOUSE_TURN ? CAT_WIN : MOUSE_WIN;
                            results[prevMouse][prevCat][prevTurn][0] = loseResult;
                            results[prevMouse][prevCat][prevTurn][1] = moves + 1;
                            queue.offer(new int[]{prevMouse, prevCat, prevTurn});
                        }
                    }
                }
            }
        }
        return results[startMouse][startCat][MOUSE_TURN];
    }

    public List<int[]> getPrevStates(int mouse, int cat, int turn) {
        List<int[]> prevStates = new ArrayList<int[]>();
        int mouseRow = mouse / cols, mouseCol = mouse % cols;
        int catRow = cat / cols, catCol = cat % cols;
        int prevTurn = turn == MOUSE_TURN ? CAT_TURN : MOUSE_TURN;
        int maxJump = prevTurn == MOUSE_TURN ? mouseJump : catJump;
        int startRow = prevTurn == MOUSE_TURN ? mouseRow : catRow;
        int startCol = prevTurn == MOUSE_TURN ? mouseCol : catCol;
        prevStates.add(new int[]{mouse, cat, prevTurn});
        for (int[] dir : dirs) {
            for (int i = startRow + dir[0], j = startCol + dir[1], jump = 1; i >= 0 && i < rows && j >= 0 && j < cols && grid[i].charAt(j) != '#' && jump <= maxJump; i += dir[0], j += dir[1], jump++) {
                int prevMouseRow = prevTurn == MOUSE_TURN ? i : mouseRow;
                int prevMouseCol = prevTurn == MOUSE_TURN ? j : mouseCol;
                int prevCatRow = prevTurn == MOUSE_TURN ? catRow : i;
                int prevCatCol = prevTurn == MOUSE_TURN ? catCol : j;
                int prevMouse = getPos(prevMouseRow, prevMouseCol);
                int prevCat = getPos(prevCatRow, prevCatCol);
                prevStates.add(new int[]{prevMouse, prevCat, prevTurn});
            }
        }
        return prevStates;
    }

    public int getPos(int row, int col) {
        return row * cols + col;
    }
}
