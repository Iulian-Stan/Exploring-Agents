package PrairieDogs;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class Action extends Observable{
	private List<Integer> _ids;
	private int _cost;
	private int _stepsTillRandomization;

	public Action(int steps)
	{
		_ids = new LinkedList<Integer>();
		_cost = 0;
		_stepsTillRandomization = 4*steps;
	}

	public int getCost()
	{
		return _cost;
	}

	public List<Integer> getIds()
	{
		return _ids;
	}

	public void addId(int id)
	{
		_ids.add(id);
	}

	public int getSteps()
	{
		return _stepsTillRandomization;
	}

	public void setSteps(int steps)
	{
		if (steps > 0)
			_stepsTillRandomization= steps;
		else
			_stepsTillRandomization = -1;
	}

	public void updateCost(int cost)
	{
		_cost += cost;
	}

	public void clearCost()
	{
		_cost = 0;
	}

	public void update()
	{
		if (_stepsTillRandomization > -1)
			--_stepsTillRandomization;
		setChanged();
		notifyObservers();
	}
}
