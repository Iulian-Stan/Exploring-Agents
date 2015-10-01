package PrairieDogs;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;


@SuppressWarnings("serial")
public class Core extends JFrame implements Observer{

	private final Sizes _cont = new Sizes(10, 5, 10, 100, 1000);
	private final Action _act = new Action(_cont.getRandomizeInterval());

	private ControlPanel _control;
	private OpenEnvironment _openEnvironement;

	public Core ()
	{
		_act.addObserver(this);
		_cont.addObserver(this);

		_openEnvironement = new OpenEnvironment(_cont, _act);
		_control = new ControlPanel(_cont);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container pane = getContentPane();
		pane.add(_control, BorderLayout.WEST);
		pane.add(_openEnvironement, BorderLayout.CENTER);
		pack();
		setVisible(true);
	}


	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Core();
			}
		});
	}


	@Override
	public void update(Observable observale, Object object) {
		if (observale == _cont)
		{
			switch (_cont.flag)
			{
			case -2 :
				_openEnvironement.setRandomizeInterval(_cont.getRandomizeInterval());
			case -1 :
				_openEnvironement.setAgentsWaitInterval(_cont.getDelayInterval());
				break;
			case 0 :
				_openEnvironement.StopAgents();
				remove(_openEnvironement);
				_openEnvironement = new OpenEnvironment(_cont, _act);
				add(_openEnvironement, BorderLayout.CENTER);
				pack();
				break;
			case 1 :
				_openEnvironement.StartAgents();
				break;
			case 2 :
				_openEnvironement.InterruptAgents();
				break;
			}
		}
		else
		{
			if (_act.getCost() == 99)
				_cont.HoleFilled();
			_control.UpdateScore(_act.getCost(), _act.getIds());
			_act.clearCost();
			if (_cont.getHolesNumber() == 0)
				_openEnvironement.InterruptAgents();
			if (_act.getSteps() == 0)
			{
				_openEnvironement.Randomize();
				_act.setSteps(_cont.getRandomizeInterval());
			}
		}
	}
}
