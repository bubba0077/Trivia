package net.bubbaland.trivia.client;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Class courtesy of David Wright via Heinz Kabutz: http://www.javaspecialists.eu/archive/Issue145.html
 */
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;

public final class TristateCheckBox extends JCheckBox {
	/**
	 *
	 */
	private static final long		serialVersionUID	= -454989429269684055L;
	// Listener on model changes to maintain correct focusability
	private final ChangeListener	enableListener		= new ChangeListener() {
															@Override
															public void stateChanged(ChangeEvent e) {
																TristateCheckBox.this.setFocusable(
																		TristateCheckBox.this.getModel().isEnabled());
															}
														};

	public TristateCheckBox(String text) {
		this(text, null, TristateState.DESELECTED);
	}

	public TristateCheckBox(String text, Icon icon, TristateState initial) {
		super(text, icon);

		// Set default single model
		this.setModel(new TristateButtonModel(initial));

		// override action behaviour
		super.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				TristateCheckBox.this.iterateState();
			}
		});
		final ActionMap actions = new ActionMapUIResource();
		actions.put("pressed", new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 847908426786835995L;

			@Override
			public void actionPerformed(ActionEvent e) {
				TristateCheckBox.this.iterateState();
			}
		});
		actions.put("released", null);
		SwingUtilities.replaceUIActionMap(this, actions);
	}

	// Next two methods implement new API by delegation to model
	public void setIndeterminate() {
		this.getTristateModel().setIndeterminate();
	}

	public boolean isIndeterminate() {
		return this.getTristateModel().isIndeterminate();
	}

	public TristateState getState() {
		return this.getTristateModel().getState();
	}

	public void setState(TristateState state) {
		this.getTristateModel().setState(state);
	}

	// Overrides superclass method
	@Override
	public void setModel(ButtonModel newModel) {
		super.setModel(newModel);

		// Listen for enable changes
		if (this.model instanceof TristateButtonModel) {
			this.model.addChangeListener(this.enableListener);
		}
	}

	// Empty override of superclass method
	@Override
	public void addMouseListener(MouseListener l) {
	}

	// Mostly delegates to model
	private void iterateState() {
		// Maybe do nothing at all?
		if (!this.getModel().isEnabled()) return;

		this.grabFocus();

		// Iterate state
		this.getTristateModel().iterateState();

		// Fire ActionEvent
		int modifiers = 0;
		final AWTEvent currentEvent = EventQueue.getCurrentEvent();
		if (currentEvent instanceof InputEvent) {
			modifiers = ( (InputEvent) currentEvent ).getModifiers();
		} else if (currentEvent instanceof ActionEvent) {
			modifiers = ( (ActionEvent) currentEvent ).getModifiers();
		}
		this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, this.getText(),
				System.currentTimeMillis(), modifiers));
	}

	// Convenience cast
	public TristateButtonModel getTristateModel() {
		return (TristateButtonModel) super.getModel();
	}

	public enum TristateState {
		SELECTED {
			@Override
			public TristateState next() {
				return INDETERMINATE;
			}
		},
		INDETERMINATE {
			@Override
			public TristateState next() {
				return DESELECTED;
			}
		},
		DESELECTED {
			@Override
			public TristateState next() {
				return SELECTED;
			}
		};

		public abstract TristateState next();
	}

	public class TristateButtonModel extends ToggleButtonModel {
		/**
		 *
		 */
		private static final long	serialVersionUID	= -8400221474441348397L;
		private TristateState		state				= TristateState.DESELECTED;

		public TristateButtonModel(TristateState state) {
			this.setState(state);
		}

		public TristateButtonModel() {
			this(TristateState.DESELECTED);
		}

		public void setIndeterminate() {
			this.setState(TristateState.INDETERMINATE);
		}

		public boolean isIndeterminate() {
			return this.state == TristateState.INDETERMINATE;
		}

		// Overrides of superclass methods
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			// Restore state display
			this.displayState();
		}

		@Override
		public void setSelected(boolean selected) {
			this.setState(selected ? TristateState.SELECTED : TristateState.DESELECTED);
		}

		// Empty overrides of superclass methods
		@Override
		public void setArmed(boolean b) {
		}

		@Override
		public void setPressed(boolean b) {
		}

		void iterateState() {
			this.setState(this.state.next());
		}

		private void setState(TristateState state) {
			// Set internal state
			this.state = state;
			this.displayState();
			if (state == TristateState.INDETERMINATE && this.isEnabled()) {
				// force the events to fire

				// Send ChangeEvent
				this.fireStateChanged();

				// Send ItemEvent
				final int indeterminate = 3;
				this.fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, indeterminate));
			}
		}

		private void displayState() {
			super.setSelected(this.state == TristateState.SELECTED);
			super.setArmed(this.state == TristateState.INDETERMINATE);
			super.setPressed(this.state == TristateState.INDETERMINATE);
		}

		public TristateState getState() {
			return this.state;
		}
	}
}