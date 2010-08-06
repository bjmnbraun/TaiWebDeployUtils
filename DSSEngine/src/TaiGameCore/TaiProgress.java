package TaiGameCore;

import java.util.ArrayList;

public class TaiProgress {
	public TaiProgress(){
		allTasks = new ArrayList();
		progresses = new ArrayList();
		expectedMax = new ArrayList();
	}
	public ArrayList<String> allTasks;
	/**
	 * NEGATIVE PROGRESS = FAILURE
	 */
	public ArrayList<Float> progresses;
	public ArrayList<Float> expectedMax;
	private String currentUpdate;
	public void mark(String name, float expectedmax){
		currentUpdate = name;
		allTasks.add(currentUpdate);
		progresses.add(0f);
		expectedMax.add(expectedmax);
	}
	public void updateState(float value){
		int index = allTasks.indexOf(currentUpdate);
		progresses.set(index,value);
	}
	public void markDone(){
		int index = allTasks.indexOf(currentUpdate);
		progresses.set(index,expectedMax.get(index));
		try {
			System.gc();
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void fail(){
		int index = allTasks.indexOf(currentUpdate);
		progresses.set(index,-1f);
	}
}
