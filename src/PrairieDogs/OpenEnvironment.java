package PrairieDogs;

import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class OpenEnvironment extends Environment {

	private Position[][] _map;
	private List<Point> _holes = new LinkedList<Point>();

	private static Random generator = new Random();

	private Agent[] _agents = new Agent[4];
	private Action _act;

	private int _holesN;

	private boolean hasNoNeighbors(int row, int column, Position refrence)
	{
		return (row - 1 < 0 || 
				(column - 1 < 0 || _map[row-1][column-1] != refrence) &&
				_map[row-1][column] != refrence &&
				(column + 1 >= N || _map[row-1][column+1] != refrence)) &&
				(column - 1 < 0 || _map[row][column-1] != refrence) &&
				(column + 1 >= N || _map[row][column+1] != refrence) &&
				(row + 1 >= N ||
				(column - 1 < 0 || _map[row+1][column-1] != refrence) &&
				_map[row+1][column] != refrence &&
				(column + 1 >= N || _map[row+1][column+1] != refrence));
	}

	private boolean isNotMarginal(int row, int column)
	{
		return row != 0 && row != N - 1 && column != 0 && column != N - 1;
	}


	private void GenerateObstacles(int obstacles, int poz, int cellNumber)
	{
		int row, column;
		while (obstacles > 0)
		{
			poz = (poz + generator.nextInt(cellNumber)) % cellNumber;
			row = poz / N;
			column = poz % N;
			if (_map[row][column] == Position.Free && hasNoNeighbors(row, column, Position.Obstacle))
			{
				_map[row][column] = Position.Obstacle;
				--obstacles;
			}
		}
	}

	private void CreateAgents()
	{
		for (int index = 0; index < _agents.length; ++index)
			_agents[index] = new Agent(this, N, index / 2 * (N - 1), index % 2 * (N - 1));
	}

	private void ExploreAgents()
	{
		for (int index = 0; index < _agents.length; ++index)
			_agents[index].Explore();
	}

	private void SetIconAgents()
	{
		for (int index = 0; index < _agents.length; ++index)
		{
			Point point = _agents[index].getInitialPoint();
			_list.get(point.getX() * N + point.getY()).setIcon(_agentIcon);
		}
	}

	private void GenerateHoles(int holes, int poz, int cellNumber)
	{
		int row, column;
		while (holes > 0)
		{
			poz = (poz + generator.nextInt(cellNumber)) % cellNumber;
			row = poz / N;
			column = poz % N;
			if (_map[row][column] == Position.Free  && hasNoNeighbors(row, column, Position.Hole))
			{
				_map[row][column] = Position.Hole;
				_holes.add(new Point(row,  column));
				--holes;
			}
		}
	}

	private void GenerateTiles(int tiles, int poz, int cellNumber)
	{
		int row, column;
		while (tiles > 0)
		{
			poz = (poz + generator.nextInt(cellNumber)) % cellNumber;
			row = poz / N;
			column = poz % N;
			if (_map[row][column] == Position.Free  && hasNoNeighbors(row, column, Position.Tile) 
					&& isNotMarginal(row, column))
			{
				_map[row][column] = Position.Tile;
				--tiles;
			}
		}
	}

	private void GenerateMap(int obstacles, int holes)
	{		
		int tiles = holes;
		int poz = 0, cellNumber = N * N - 1;

		_holesN = holes;

		GenerateObstacles(obstacles, poz, cellNumber);

		GenerateHoles(holes, poz, cellNumber);

		GenerateTiles(tiles, poz, cellNumber);
	}

	public void Randomize()
	{
		int row, column, holes = _holesN;
		if (holes > 0)
		{
			InterruptAgents();
			JLabel label;
			for(Point position : _holes)
			{
				row = position.getX();
				column = position.getY();
				_map[row][column] = Position.Free;
				label = (JLabel)getComponent(row * N + column);
				label.setIcon(_freeIcon);
			}
			_holes.clear();
			_holes = new LinkedList<Point>();
			GenerateHoles(holes, 0, N * N - 1);
			for(Point position : _holes)
			{
				row = position.getX();
				column = position.getY();
				label = (JLabel)getComponent(row * N + column);
				label.setIcon(_holeIcon);
			}
			StartAgents();
		}
	}

	public OpenEnvironment(Sizes sizes, Action act) {
		int row, column;

		N = sizes.getSize();
		_map = new Position[N][N];
		CreateAgents();
		_act = act;

		this.setLayout(new GridLayout(N, N));

		for (row = 0; row < N; ++row)
			for (column = 0; column < N; ++column)
				_map[row][column] = Position.Free;
		for (int index = 0; index < _agents.length; ++index)
			_map[_agents[index].getInitialPoint().getX()][_agents[index].getInitialPoint().getY()] = Position.Agent;

		GenerateMap(sizes.getObstaclesNumber(), sizes.getHolesNumber());

		ExploreAgents();


		for (row = 0; row < N; ++row) {
			for (column = 0; column < N; column++) {
				switch (_map[row][column])
				{
				case Free:
					_list.add(new JLabel(_freeIcon));
					break;
				case Hole:
					_list.add(new JLabel(_holeIcon));
					break;
				case Tile:
					_list.add(new JLabel(_tileIcon));
					break;
				default :
					_list.add(new JLabel());
					break;
				}
			}
		}

		SetIconAgents();

		for (JLabel label : _list) 
			add(label);
		validate();

		setAgentsWaitInterval(sizes.getDelayInterval());
		for (int index = 0; index < _agents.length; ++index)
			(new Thread(_agents[index])).start();
	}

	public Position getPosition(Point point)
	{
		return getPosition(point.getX(), point.getY());
	}

	public Position getPosition(int row, int column)
	{
		if (row < 0 || row >= N || column < 0 || column >= N)
			return Position.Unexistent;
		return _map[row][column];
	}

	public void StartAgents()
	{
		for (int index = 0; index < _agents.length; ++index)
			_agents[index].Run();
	}

	public void InterruptAgents()
	{		
		for (int index = 0; index < _agents.length; ++index)
			_agents[index].Stop();
	}

	public void StopAgents()
	{
		for (int index = 0; index < _agents.length; ++index)
			_agents[index].Finish();
	}

	public void setAgentsWaitInterval(int delay)
	{
		for (int index = 0; index < _agents.length; ++index)
			_agents[index].setDelayInterval(delay);
	}

	public void setRandomizeInterval(int steps)
	{
		_act.setSteps(steps);
	}

	protected synchronized void Update(Changes changes) {
		Point previous = changes.getPreviousPoint(), 
				current = changes.getCurrentPoint();  
		int row = previous.getX(), column = previous.getY();

		//Update previous position
		_map[row][column] = changes.getPosition();
		JLabel label = (JLabel)getComponent(row * N + column);
		//It may be a filled hole or a free space
		switch (changes.getPosition())
		{
		case FilledHole:
			label.setIcon(_filledHoleIcon);
			break;
		default:
			label.setIcon(_freeIcon);
		}

		//If the new position is a tile - push it
		if (_map[current.getX()][current.getY()] == Position.Tile)
		{
			_map[current.getX()][current.getY()] = Position.Free;
			row = 2 * current.getX() - row;
			column = 2 * current.getY() - column;
			label = (JLabel)getComponent(row * N + column);
			if (_map[row][column] == Position.Hole)
			{
				_map[row][column] = Position.FilledHole;
				label.setIcon(_filledHoleIcon);
				--_holesN;
				_act.updateCost(100);
			}
			else
			{
				//If a tile pass over a filled hole it will erase it
				_map[row][column] = Position.Tile;
				label.setIcon(_tileIcon);
			}
		}

		//Update new position
		row = current.getX();
		column = current.getY();
		changes.setPosition(_map[row][column]);
		_map[row][column] = Position.Agent;
		label = (JLabel)getComponent(row * N + column);
		label.setIcon(_agentIcon);
		_act.updateCost(-1);
		for (int index = 0; index < _agents.length; ++index)
			if (_agents[index].getInitialPoint().equals(changes.getCurrentPoint()))
				_act.addId(index);
		_act.update();
	}
}