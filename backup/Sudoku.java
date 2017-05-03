import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

class Myframe extends JFrame {// 继承界面类
public static Object obj = new Object();
public final static JTextField[][] filed = new JTextField[9][9];// 创建九宫格界面

public Myframe() {// 初始化界面，让所有的格子都等于空
	for (int a = 0; a < 9; a++) {
	 for (int b = 0; b < 9; b++) {
		filed[a][b] = new JTextField();
		filed[a][b].setText("");
	 }
	}
	JPanel jpan = new JPanel();// 编写布局，把textfield添加到布局中
	jpan.setLayout(new GridLayout(9, 9));
	for (int a = 8; a > -1; a--) {
	 for (int b = 0; b < 9; b++) {
		jpan.add(filed[b][a]);
	 }
	}
	add(jpan, BorderLayout.CENTER);// 界面布局为居中
	JPanel jpb = new JPanel();
	JButton button1 = new JButton("求解");// 设置两个按钮，计算和退出
	JButton button2 = new JButton("关闭");
	jpb.add(button1);// 将按钮添加到界面上
	jpb.add(button2);
	button1.addActionListener(new ActionListener() {// 给按钮添加监听器，就是添加事件响应函数
	public void actionPerformed(ActionEvent event) {
	synchronized (obj) {
	 for (int a = 0; a < 9; a++) {
		for (int b3 = 0; b3 < 9; b3++) {
		 int pp = 0;
		 if (!(filed[a][b3].getText().trim().equals(""))) {// 获取九宫格中的已填入数据的值，这些就是谜面
			pp = Integer.parseInt(filed[a][b3].getText()
					.trim());
			Calculate.b[a][b3] = pp;
		 }
		}
	}
	}
	synchronized (obj) {
		new Thread(new Calculate()).start();// 开启线程计算九宫格的答案
	}
	}
	});
	button2.addActionListener(new ActionListener() {// button2很简单，调用api关闭程序
		public void actionPerformed(ActionEvent event) {
			System.exit(0);
		}
	});
	add(jpb, BorderLayout.SOUTH);// 设置界面的布局
}
}

public class Sudoku {
public static void main(String[] args) {
	Myframe myf = new Myframe();
	myf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	myf.setTitle("Sudoku");// 设置主界面的名称
	myf.setSize(500, 500);// 设置界面的大小
	myf.setVisible(true);// 设置主程序可见
	}
}

class Calculate implements Runnable {
	public static boolean[][] boo = new boolean[9][9];
	public static int upRow = 0;
	public static int upColumn = 0;
	public static int[][] b = new int[9][9];

	public static void flyBack(boolean[][] judge, int row, int column) {
		int s = column * 9 + row;// 生成临时变量s，具体下面会介绍
		s--;
		int quotient = s / 9;// 取商的值，实际就是column的值
		int remainder = s % 9;// 取余数的值，实际是取(row-1)%9
		if (judge[remainder][quotient]) {// 判断是否满足条件
			flyBack(judge, remainder, quotient);
		} else {
			upRow = remainder;// 赋值给upRow
			upColumn = quotient;// 赋值给upColumn
		}
	}

  public static void arrayAdd(ArrayList<Integer> array, TreeSet<Integer> tree) {
	for (int z = 1; z < 10; z++) {// 遍历1~10
		boolean flag3 = true;// flag3默认为true，判断z是否符合条件
		Iterator<Integer> it = tree.iterator();// it就是一个迭代器
		while (it.hasNext()) {// tree如果没有遍历完继续遍历
			int b = it.next().intValue();// 将列表中的值赋给b
			if (z == b) {
				flag3 = false;
				break;
			}
		}
		if (flag3) {
			array.add(new Integer(z));// 如果判断z没有出现在过tree中，就将它添加进去
		}
		flag3 = true;// 初始化flag3
	}
  }

  public static ArrayList<Integer> assume(int row, int column) {
	ArrayList<Integer> array = new ArrayList<Integer>();// 创建数组array
	TreeSet<Integer> tree = new TreeSet<Integer>();
	for (int a = 0; a < 9; a++) { // 添加同一列其他的元素值
		if (a != column && b[row][a] != 0) { // 如果该格不为空，就添加到tree中
			tree.add(new Integer(b[row][a]));
		}
	}
	for (int c = 0; c < 9; c++) {// 添加同行的其他元素
		if (c != row && b[c][column] != 0) {// 如果该格满足添加，就添加到tree中
			tree.add(new Integer(b[c][column]));
		}
	}
	for (int a = (row / 3) * 3; a < (row / 3 + 1) * 3; a++)// 这里使用了整型除法只保留整数部分的特点，获取元素在同一个小九宫格的行，
	{
	  for (int c = (column / 3) * 3; c < (column / 3 + 1) * 3; c++) {// 获取元素在同一个九宫格的列
		if ((!(a == row && c == column)) && b[a][c] != 0) {// 如果元素满足条件都添加到tree中
			tree.add(new Integer(b[a][c]));
		}
	  }
	}
	arrayAdd(array, tree);
	return array;
  }

  public void run() {
	int row = 0,column = 0; // 初始化变量行,列
	boolean flag = true;//flag用来判断该格子是否填入正确
	for (int a = 0; a < 9; a++) {
	 for (int c = 0; c < 9; c++) {
		if (b[a][c] != 0) {
			boo[a][c] = true;// boo的作用是找出填入数据的空格，填入数据的空格是谜面，我们需要根据这些信息解迷题
		} else {
			boo[a][c] = false;// 为空的格子是需要填入数据的部分
		}
	 }
	}
	ArrayList<Integer>[][] utilization = new ArrayList[9][9];// arraylist是一个二维的序列，它的每一个值都是一个数组指针，存放了该格子可能的解，当一个解错误时，调用下一个解，这也就是前面介绍的数独解法
	while (column < 9) {
		if (flag == true) {
			row = 0;//当row循环到9后就 
		}
	while (row < 9) {
	if (b[row][column] == 0) {
	if (flag) {
		ArrayList<Integer> list = assume(row, column);//
		utilization[row][column] = list;
	}
	if (utilization[row][column].isEmpty()) {// 如果没有找到可能的解，说明前面的值有错误，就回溯到之前的格子进行修改
		flyBack(boo, row, column); // 调用flyBack函数寻找合适的row和column
		row = upRow;// 将row返回到合适的位子
		column = upColumn;// 将column返回到合适的位子
		b[row][column] = 0;// 初始化有问题的格子
		column--;
		flag = false;
		break;
	} else {
		b[row][column] = utilization[row][column].get(0);// 将备选数组中第一个值赋给b
		utilization[row][column].remove(0);// 因为上面已经赋值了，所以就删除掉第一个数值
		flag = true;
		judge();//判断是否所有的格子都填入正确，然后将正确的结果输出到屏幕上
	  }
	} else {
		flag = true;
	}// 如果r为false，说明还有格子没填入数据，就继续遍历
	row++;
	}
	column++;
  }
 }
  public void judge()
  {
	boolean r = true;
	for (int a1 = 0; a1 < 9; a1++) {// 查找还没有填入数据的格子
	  for (int b1 = 0; b1 < 9; b1++) {
		if (r == false) {
			break;
		}
		if (b[a1][b1] == 0) {// 如果(b[a1][b1]需要计算，就将它提取出来
			r = false;
		}
	}
	}
	if (r) { // 如果r为true，则所有的格子都填入了数据，说明九宫格就完成了，此时输出结果到屏幕上
	  for (int a1 = 0; a1 < 9; a1++) {
		for (int b1 = 0; b1 < 9; b1++) {
			Myframe.filed[a1][b1].setText(b[a1][b1]
					+ "");
		}
	}
	}
  }
}