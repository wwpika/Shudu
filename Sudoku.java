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

//继承界面类
class Myframe extends JFrame{
	public static Object obj=new Object();
	//创建九宫格
	public final static JTextField[][] filed=new JTextField[9][9];

	public Myframe(){
		//初始化界面,让所有的格子都等于空
		for(int a=0;a<9;a++){
			for(int b=0;b<9;b++){
				filed[a][b]=new JTextField();
				filed[a][b].setText("");
			}
		}

		//写布局，把textfield添加到布局中
		JPanel jpan=new JPanel();
		jpan.setLayout(new GridLayout(9,9));
		for(int a=8;a>-1;a--){
			for(int b=0;b<9;b++){
				jpan.add(filed[b][a]);
			}
		}

		//界面布局为居中
		add(jpan,BorderLayout.CENTER);
		JPanel jpb=new JPanel();

		JButton button1=new JButton("求解");
		JButton button2=new JButton("关闭");

		//将按钮添加到界面上
		jpb.add(button1);
		jpb.add(button2);

		//添加事件响应函数
		button1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				synchronized(obj){
					for(int a=0;a<9;a++){
						for(int b3=0;b3<9;b3++){
							int pp=0;
							//获取九宫格中已填入的数据
							if(!(filed[a][b3].getText().trim().equals(""))){
								pp=Integer.parseInt(filed[a][b3].getText()
									.trim());
								Calculate.b[a][b3]=pp;
							}
						}
					}
				}

				synchronized(obj){
					//开启线程计算答案
					new Thread(new Calculate()).start();
				}
			}

		});

		button2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				System.exit(0);
			}
		});

		//设置2个按钮的界面布局
		add(jpb,BorderLayout.SOUTH);
	}
}

public class Sudoku{
	public static void main(String[] args){
		Myframe myf=new Myframe();
		myf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		myf.setTitle("Sudoku");
		myf.setSize(500,500);

		myf.setVisible(true);
	}
}

class Calculate implements Runnable{
	//用来判断该格是否为空
	public static boolean[][] boo=new boolean[9][9];

	//计算指定行的值
	public static int upRow=0;

	//计算指定列的值
	public static int upColumn=0;

	//存储九宫格中的数据
	public static int[][] b=new int[9][9];


	//找出同一列的上一行的非迷面的数据
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

	//找出候选解，将其放在ArrayList中
	public static void arrayAdd(ArrayList<Integer> array, TreeSet<Integer> tree){
		for(int z=1;z<10;z++){
			//flag3默认为true，判断z是否符合条件
			boolean flag3=true;

			//it就是一个迭代器
			Iterator<Integer> it=tree.iterator();

			while(it.hasNext()){
				int b=it.next().intValue();
				if(z==b){
					flag3=false;
					break;
				}
			}

			//如果z没有出现在tree中，就将它添加进去
			if(flag3){
				array.add(new Integer(z));
			}

			flag3=true;
		}
	}


	//将某个元素的同行同列同一个小九宫格内的已被填充的数，放到treeset中
	public static ArrayList<Integer> assume(int row, int column){
		ArrayList<Integer> array=new ArrayList<Integer>();
		TreeSet<Integer> tree=new TreeSet<Integer>();

		//添加同一行的其他的元素
		for(int a=0;a<9;a++){
			if(a!=column && b[row][a]!=0){
				tree.add(new Integer(b[row][a]));
			}

		}

		//添加同一列的其他元素的数据
		for(int c=0;c<9;c++){
			if(c!=row && b[c][column]!=0){
				tree.add(new Integer(b[c][column]));
			}
		}

		//a代表元素所在小九宫格的行的范围
		for(int a=(row/3)*3;a<(row/3+1)*3;a++)
		{
			//c代表元素所在小九宫格的列的范围
			for(int c=(column/3)*3;c<(column/3+1)*3;c++)
			{
				if(!(a==row&&c==column) && b[a][c]!=0){
					tree.add(new Integer(b[a][c]));
				}

			}

		}

		//找出候选解
		arrayAdd(array,tree);
		return array;	
	}


	public void run()
	{

    	// 初始化变量行，列
    	int row = 0,column = 0; 

    	// flag用来判断该格子是否填入正确
    	boolean flag = true;
    	for (int a = 0; a < 9; a++) {
        	for (int c = 0; c < 9; c++) {
            	if (b[a][c] != 0)
            	{
                /* boo的作用是找出填入数据的空格，
                *  填入数据的空格是谜面，我们需要根据这些信息解迷题
                */
                	boo[a][c] = true;
            	}
            	else
            	{
                // 为空的格子是需要填入数据的部分
                	boo[a][c] = false;
           	 	}	
        	}	
    	}

    /* arraylist是一个二维的序列，它的每一个值都是一个数组指针，
    *  存放了该格子可能的解，当一个解错误时，调用下一个解，
    * 这也就是前面介绍的数独解法。
    */
    	ArrayList<Integer>[][] utilization = new ArrayList[9][9];

    	while (column < 9)
    	{
        	if (flag == true) 
        	{
           		row = 0;
        	}
        	while (row < 9) 
        	{
            	if (b[row][column] == 0)
            	{
                	if (flag) 
                	{
                    	ArrayList<Integer> list = assume(row, column);//
                    	utilization[row][column] = list;
                	}

                	// 如果没有找到可能的解，说明前面的值有错误，就回溯到之前的格子进行修改
                	if (utilization[row][column].isEmpty()) {
                    	// 调用flyBack函数寻找合适的row和column
                    	flyBack(boo, row, column); 

                    	// 将row返回到合适的位子
                    	row = upRow;

                    	// 将column返回到合适的位子
                    	column = upColumn;

                    	// 初始化有问题的格子
                    	b[row][column] = 0;
                    	column--;
                    	flag = false;
                    	break;
                	} 
                	else 
                	{
                    	// 将备选数组中第一个值赋给b
                    	b[row][column] = utilization[row][column].get(0);

                    	// 因为上面已经赋值了，所以就删除掉第一个数值
                    	utilization[row][column].remove(0);
                    	flag = true;

                    	//判断是否所有的格子都填入正确，然后将正确的结果输出到屏幕上
                    	judge();
                	}
            	} 
            	else 
            	{
                	// 如果r为false，说明还有格子没填入数据，就继续遍历
                	flag = true;
            	}
        		row++;
        	}
        	column++;
        
    	}
	}

	public void judge()
	{
    	boolean r = true;

    	// 查找还没有填入数据的格子,若r=false，最终会退出for循环
    	for (int a1 = 0; a1 < 9; a1++) {
        	for (int b1 = 0; b1 < 9; b1++) {
            	if (r == false) {
                	break;
            	}

            	// 如果 b[a1][b1] 需要计算，就将它提取出来
            	if (b[a1][b1] == 0) {
                	r = false;
            	}
       	 	}
    	}

    	// 如果r为true，则所有的格子都填入了数据，说明九宫格就完成了，此时输出结果到屏幕上
    	if (r) { 
        	for (int a1 = 0; a1 < 9; a1++) {
            	for (int b1 = 0; b1 < 9; b1++) {
                	Myframe.filed[a1][b1].setText(b[a1][b1] + "");
            	}
        	}
    	}
	}
	

}