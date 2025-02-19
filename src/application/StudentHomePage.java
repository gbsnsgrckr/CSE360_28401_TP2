package application;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tests.PopulateQADatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import databasePart1.DatabaseHelper;

/**
 * This page displays a simple welcome message for the user.
 */
public class StudentHomePage {
	private final DatabaseHelper databaseHelper;
	private Question question;
	private Answer answer;
	private List<Question> questions;
	private List<Answer> answers;
	private List<QuestionsSet> questionsSet;
	private List<AnswersSet> answersSet;
	private ObservableList<QATableRow> resultsObservableList = FXCollections.observableArrayList();
	private TableView<Question> qTable;
	private TableView<QATableRow> resultsTable;

	public StudentHomePage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	// Class to allow for dynamic rows within a table
	public class QATableRow {
		public enum RowType {
			QUESTION, ANSWER
		}

		private final RowType type;
		private final String text;
		private final Integer answerId;
		private final Integer authorId;

		public QATableRow(RowType type, String text, Integer answerId, Integer authorId) {
			this.type = type;
			this.text = text;
			this.answerId = answerId;
			this.authorId = authorId;
		}

		public QATableRow(RowType type, String text, Integer answerId) {
			this.type = type;
			this.text = text;
			this.answerId = answerId;
			this.authorId = null;
		}

		public RowType getType() {
			return type;
		}

		public String getText() {
			return text;
		}

		public Integer getAnswerId() {
			return answerId;
		}

		public Integer getAuthorId() {
			return authorId;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public void show(Stage primaryStage) {

		// Use statement below to populate databases
		// new PopulateQADatabase(databaseHelper.qaHelper).execute();

		try {
			questions = databaseHelper.qaHelper.getAllQuestions();
		} catch (SQLException e) {
			System.out.println("Should never reach here, can't get all QUESTIONS");
			e.printStackTrace();
		}

		try {
			answers = databaseHelper.qaHelper.getAllAnswers();
		} catch (SQLException e) {
			System.out.println("Should never reach here, can't get all ANSWERS");
			e.printStackTrace();
		}

		/*
		 * // Debug to check database contents try {
		 * System.out.println("Fetching all questions..."); List<Question>
		 * questionsFromDb = databaseHelper.getAllQuestions(); if (questionsFromDb ==
		 * null || questionsFromDb.isEmpty()) {
		 * System.out.println("No questions found in the database."); } else {
		 * System.out.println("Questions found: " + questionsFromDb.size());
		 * System.out.println(questionsFromDb); }
		 * 
		 * System.out.println("Fetching all answers..."); List<Answer> answersFromDb =
		 * databaseHelper.getAllAnswers(); if (answersFromDb == null ||
		 * answersFromDb.isEmpty()) {
		 * System.out.println("No answers found in the database."); } else {
		 * System.out.println("Answers found: " + answersFromDb.size());
		 * System.out.println(answersFromDb); } } catch (SQLException e) {
		 * e.printStackTrace();
		 * System.err.println("Error trying to check contents of databases"); ; return;
		 * }
		 */

		// Label to display title of area to the user
		Label prompt = new Label("Entry Box");
		prompt.setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;");

		// Hbox to hold and position the title
		HBox titleBox = new HBox(prompt);
		// Set alignment of box
		titleBox.setAlignment(Pos.CENTER);

		// Input box for a title
		TextArea titleField = new TextArea();
		titleField.setPromptText("Enter a title for your question. This should be a question");
		// Styling for the titleField
		titleField.setStyle("-fx-text-fill: black; -fx-font-weight: bold;-fx-border-color: black, gray;"
				+ "-fx-border-width: 2, 1; -fx-border-radius: 3, 1; -fx-border-inset: 0, 4;");
		titleField.setMaxWidth(600);
		titleField.setPrefWidth(600);
		titleField.setMaxHeight(20);

		// Input box for body of question
		TextArea inputField = new TextArea();
		inputField.setPromptText("Ask your question in more detail");
		// Styling for the inputField
		inputField.setStyle("-fx-text-fill: black; -fx-font-weight: bold;-fx-border-color: black, gray;"
				+ "-fx-border-width: 2, 1; -fx-border-radius: 3, 1; -fx-border-inset: 0, 4;");
		inputField.setMaxWidth(600);
		inputField.setPrefWidth(600);
		inputField.setMaxHeight(300);
		inputField.setPrefHeight(300);
		inputField.setWrapText(true);

		// Button search using text in input fields
		Button deleteButton = new Button("Delete");
		deleteButton.setStyle(
				"-fx-text-fill: black; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width:  1;");

		// Button to submit question text in input fields to database
		Button submitButton = new Button("Submit Question");
		submitButton.setStyle(
				"-fx-text-fill: black; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width:  1;");

		// Button to submit answer text in input fields to database
		Button submitAnswerButton = new Button("Submit Answer");
		submitAnswerButton.setStyle(
				"-fx-text-fill: black; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width:  1;");

		// Button to return to the login screen
		Button quitButton = new Button("Back to login");
		quitButton.setStyle(
				"-fx-text-fill: black; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width:  1;");

		// Container to hold search and submit boxes horizontally to each other
		HBox buttonBox = new HBox(5, submitButton, submitAnswerButton, deleteButton);
		buttonBox.setAlignment(Pos.CENTER);

		// Container to hold the two input boxes for questions and their title together
		VBox questionInputBox = new VBox(5, titleBox, titleField, inputField, buttonBox);

		// Table display of the question database
		// Create table to display the question database within
		TableView<Question> qTable = new TableView<>();
		// Styling for the table
		qTable.setMinWidth(300);
		qTable.setFixedCellSize(-1);

		qTable.setRowFactory(a -> new TableRow<Question>() {
			@Override
			protected void updateItem(Question item, boolean flag) {
				super.updateItem(item, flag);
				if (flag || item == null) {
					setStyle("-fx-border-color: transparent;");
				} else {
					setStyle(
							"-fx-text-fill: black; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width:  2px; -fx-table-cell-border-color: black;");
				}
			}
		});

		// Create an observable list of questions and assign to the table
		ObservableList<Question> questionObservableList = FXCollections.observableArrayList(questions);
		qTable.setItems(questionObservableList);

		TableColumn<Question, String> detailsColumn = new TableColumn<>("Question Details");
		detailsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().toDisplay()));

		// Add cell factory to deal with text runoff and disable horizontal scrolling
		detailsColumn.setCellFactory(a -> new TableCell<Question, String>() {
			private final Label textLabel = new Label();

			{
				textLabel.setWrapText(false);
				textLabel.setEllipsisString("...");
				textLabel.setMaxWidth(Double.MAX_VALUE);
				textLabel.setStyle("-fx-text-overrun: ellipsis; -fx-padding: 5px;");
			}

			@Override
			protected void updateItem(String item, boolean flag) {
				super.updateItem(item, flag);
				setGraphic(flag || item == null ? null : textLabel);
				if (!flag && item != null) {
					textLabel.setText(item);
					textLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
				}
			}
		});

		qTable.getColumns().addAll(detailsColumn);

		// Listener to dynamically hide the title bar of the Question Details table
		qTable.widthProperty().addListener((obs, oldVal, newVal) -> {
			Node titleBar = qTable.lookup("TableHeaderRow");
			if (titleBar != null && titleBar.isVisible()) {
				titleBar.setVisible(false);
				titleBar.setManaged(false);
				titleBar.setStyle("-fx-pref-height: 0; -fx-min-height: 0; -fx-max-height: 0;");
			}
		});

		// Table display of the answer database
		// Label to display title to user
		Label prompt3 = new Label("Answer Database");
		prompt3.setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;");

		// Hbox to position the title
		HBox titleBox3 = new HBox(prompt3);
		titleBox3.setAlignment(Pos.CENTER);

		// Create table to display the answer database
		TableView<Answer> aTable = new TableView<>();
		aTable.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width:  1;");
		aTable.setPrefWidth(600);

		// if answers is null then initialize as an empty list
		if (answers == null) {
			answers = new ArrayList<>();
		}

		// Create an observable list and assign it to the table
		ObservableList<Answer> answerObservableList = FXCollections.observableArrayList(answers);
		aTable.setItems(answerObservableList);

		// Create, assign, and associate values to table
		TableColumn<Answer, Integer> idColumn2 = new TableColumn<>("Answer ID");
		idColumn2.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));

		// Create a text column
		TableColumn<Answer, String> textColumn2 = new TableColumn<>("Answer");
		textColumn2.setCellValueFactory(new PropertyValueFactory<>("text"));

		// Create a userID column
		TableColumn<Answer, Integer> authorColumn2 = new TableColumn<>("Author ID");
		authorColumn2.setCellValueFactory(new PropertyValueFactory<>("author"));

		// Create a createOn column
		TableColumn<Answer, String> createdColumn2 = new TableColumn<>("Created On");
		createdColumn2.setCellValueFactory(new PropertyValueFactory<>("createdOn"));

		// Create an updatedOn column
		TableColumn<Answer, String> updatedColumn2 = new TableColumn<>("Updated On");
		updatedColumn2.setCellValueFactory(new PropertyValueFactory<>("updatedOn"));

		aTable.getColumns().addAll(idColumn2, textColumn2, authorColumn2, createdColumn2, updatedColumn2);

		// Container to hold the table
		VBox answerDB = new VBox(5, titleBox3, aTable);

		// Area to display results of searches

		// Label to display title to user
		Label prompt5 = new Label("Details");
		prompt5.setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;");

		// Hbox to position the title
		HBox titleBox5 = new HBox(prompt5);
		titleBox5.setAlignment(Pos.CENTER);

		resultsTable = new TableView<>();
		resultsTable.setItems(resultsObservableList);
		resultsTable.setRowFactory(a -> new TableRow<QATableRow>() {
			@Override
			protected void updateItem(QATableRow item, boolean flag) {
				super.updateItem(item, flag);
				if (flag || item == null) {
					setStyle("-fx-border-color: transparent;");
				} else {
					setStyle(
							"-fx-text-fill: black; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width:  2px; -fx-table-cell-border-color: black;");
				}
			}
		});

		TableColumn<QATableRow, String> contentColumn = new TableColumn<>("Results");
		contentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getText()));

		// Add cell factory to deal with text runoff and disable horizontal scrolling
		contentColumn.setCellFactory(a -> new TableCell<QATableRow, String>() {
			private final Label label = new Label();
			private final Button replyButton = new Button("Reply");
			private final TextArea replyArea = new TextArea();
			private final Button submitReplyButton = new Button("Submit");
			private final VBox replyBox = new VBox(5, submitReplyButton, replyArea);
			private final VBox cellContent = new VBox(5);

			{
				label.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
				replyButton.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");

				// Set replyBox invisible to start
				//replyBox.setVisible(false);
				//replyBox.setManaged(false);				

				// Set prompt text for replyArea
				replyArea.setPromptText("Enter your answer here...");
				replyArea.setPrefRowCount(3);				

				/*
				// Show the replyBox when replyButton is pressed
				replyButton.setOnAction(a -> {
					//boolean flag = !replyBox.isVisible();
					replyBox.setVisible(!replyBox.isVisible());
					replyBox.setManaged(!replyBox.isVisible());
				});
				*/

				submitReplyButton.setOnAction(a -> {
					String inputText = replyArea.getText().trim();
					QATableRow.RowType rowType = getTableView().getItems().get(getIndex()).getType();
					if (!inputText.isEmpty()) {
						try {							
							if (rowType == QATableRow.RowType.QUESTION) {
								databaseHelper.qaHelper.registerAnswerWithQuestion(
										new Answer(inputText, databaseHelper.currentUser.getUserId()),
										question.getId());
								replyArea.clear();
							} else {
								databaseHelper.qaHelper.registerAnswerWithAnswer(
										new Answer(inputText, databaseHelper.currentUser.getUserId()),
										getTableView().getItems().get(getIndex()).getAnswerId());
								replyArea.clear();
							}
						} catch (SQLException e) {
							e.printStackTrace();
							System.err
									.println("Error trying to register answer in results table via submitReplyButton");
						}
					}
				});
				cellContent.getChildren().addAll(label, replyBox);
				cellContent.setAlignment(Pos.CENTER_LEFT);
				
				// Update results table
				//updateResultsTableForQuestion(question);
			}

			@Override
			protected void updateItem(String item, boolean flag) {
				super.updateItem(item, flag);
				
				// Clear container
				cellContent.getChildren().clear();

				// Clear graphic
				setGraphic(null);
				if (flag && item == null) {
					setText(null);
				} else {
					cellContent.getChildren().addAll(label);
					// Get current QATableRow
					QATableRow row = getTableView().getItems().get(getIndex());
					// Create a label to hold the text
					Label textLabel = new Label(item);
					textLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
					
					// Add components to container
					cellContent.getChildren().addAll(replyBox);	//
					
					// Check if the currentUser matches the author of the answer in the cell
					if (row.getType() == QATableRow.RowType.ANSWER && row.getAuthorId() != null
							&& row.getAuthorId().equals(databaseHelper.currentUser.getUserId())) {
						// Buttons to edit and delete the answer
						Button editButton = new Button("Edit");
						Button deleteButton = new Button("Delete");
						// Styling for buttons
						editButton.setStyle(
								"-fx-background-color: transparent; -fx-background-insets: 0; -fx-border-color: black; -fx-text-fill: black; -fx-font-size: 12px;"
										+ "-fx-font-weight: bold; -fx-padding: 0;");
						deleteButton.setStyle(
								"-fx-background-color: transparent; -fx-background-insets: 0; -fx-border-color: black; -fx-text-fill: black; -fx-font-size: 12px;"
										+ "-fx-font-weight: bold; -fx-padding: 0;");

						editButton.setOnAction(a -> {
							// Edit selected answer
							// Add popup with TextArea that prepopulates the current answer text and has a
							// submit and cancel button
						});

						deleteButton.setOnAction(a -> {
							// Delete selected answer
							databaseHelper.qaHelper.deleteAnswer(row.getAnswerId());

							try {
								// Retrieve an updated list of questions from the database
								question = qTable.getSelectionModel().getSelectedItem();
								questions = databaseHelper.qaHelper.getAllQuestions();
							} catch (SQLException e) {
								e.printStackTrace();
								System.err.println(
										"Error trying update answers object via getALLUsers() in resultsTable");
							}

							// Refresh contents of tables manually
							questionObservableList.clear();
							questionObservableList.addAll(questions);
							qTable.setItems(questionObservableList);

							// Update results table
							//updateResultsTableForQuestion(question);
						});

						HBox buttonBox = new HBox(5, editButton, deleteButton);
						buttonBox.setAlignment(Pos.BOTTOM_RIGHT);

						label.setAlignment(Pos.CENTER_LEFT);

						HBox resultsHbox = new HBox(5, textLabel, buttonBox);

						cellContent.getChildren().add(resultsHbox);

						setGraphic(cellContent);
						setText(null);
					} else {
						cellContent.getChildren().add(0, textLabel);
						setGraphic(cellContent);
						setText(null);
					}

				}
			}
		});

		// Set columns to resultsTable
		resultsTable.getColumns().setAll(contentColumn);
		// Hide header for resultsTable
		resultsTable.widthProperty().addListener((obs, oldVal, newVal) -> {
			Node titleBar = resultsTable.lookup("TableHeaderRow");
			if (titleBar != null) {
				titleBar.setVisible(false);
				titleBar.setManaged(false);
				titleBar.setStyle("-fx-pref-height: 0; -fx-min-height: 0; -fx-max-height: 0;");
			}
		});

		// Create filter button to adjust question table database view
		ToggleGroup filter = new ToggleGroup();

		RadioButton allButton = new RadioButton("All");
		allButton.setToggleGroup(filter);
		allButton.setSelected(true);

		RadioButton unansweredButton = new RadioButton("Unanswered");
		unansweredButton.setToggleGroup(filter);
		;

		RadioButton answeredButton = new RadioButton("Answered");
		answeredButton.setToggleGroup(filter);

		HBox filterBox = new HBox(10, allButton, unansweredButton, answeredButton);
		filterBox.setAlignment(Pos.CENTER);

		filter.selectedToggleProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				RadioButton selected = (RadioButton) newSelection;
				String selection = selected.getText();

				try {
					if (selection.equalsIgnoreCase("All")) {
						questions = databaseHelper.qaHelper.getAllQuestions();
					} else if (selection.equalsIgnoreCase("Unanswered")) {
						questions = databaseHelper.qaHelper.getAllUnansweredQuestions();
					} else if (selection.equalsIgnoreCase("Answered")) {
						questions = databaseHelper.qaHelper.getAllAnsweredQuestions();
					}
				} catch (SQLException e) {
					e.printStackTrace();
					System.err.println("Error trying to update question table via radio buttons");
					return;
				}
				questionObservableList.setAll(questions);
				qTable.setItems(questionObservableList);
				qTable.refresh();
			}
		});

		// Hbox to position the filter button
		HBox titleBox2 = new HBox(5, filterBox);
		titleBox2.setAlignment(Pos.CENTER_RIGHT);

		// Container to hold the table
		VBox questionDB = new VBox(5, titleBox2, qTable);

		// Set height of table to adjust to container
		qTable.prefHeightProperty().bind(questionDB.heightProperty());

		// Label to display error messages
		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 20px;");
		errorLabel.setTranslateY(22);

		// "Back to login" button will bring user back to the login screen
		quitButton.setOnAction(a -> {

			// Create new stage to get rid of transparency for following pages
			Stage newStage = new Stage();
			newStage.initStyle(StageStyle.TRANSPARENT);

			// Close the existing stage
			primaryStage.close();

			new UserLoginPage(databaseHelper).show(newStage);
		});

		// Event to allow searching of question database for similar entries while the
		// user types into the inputField
		inputField.setOnKeyReleased(a -> {
			String input = inputField.getText().trim();
			if (!input.isEmpty()) {
				// Get list words from current text input string
				List<String> entry = databaseHelper.qaHelper.textDeserial(input);
				try {
					questions = databaseHelper.qaHelper.getAllQuestions();
				} catch (SQLException e) {
					e.printStackTrace();
					System.err.println("Error trying to .getAllQuestions() within inputField .setOnKeyReleased()");
					return;
				}

				// Use Hashmap to remove duplicates
				Map<Question, Integer> similarity = new HashMap<>();

				for (Question question : questions) {
					// Get list of words to compare from current question
					List<String> compList = question.getComp();
					int count = 0;

					// Count the matches
					for (String word : entry) {
						if (compList.contains(word)) {
							count++;
						}
					}

					// Set initial threshold to add comp to map
					if (count > 2) {
						similarity.put(question, count);
					}
				}

				// Sort based on similarity score
				List<Question> sortedList = similarity.entrySet().stream()
						.sorted(Map.Entry.<Question, Integer>comparingByValue().reversed()).map(Map.Entry::getKey)
						.collect(Collectors.toList());

				// Create QuestionsSet to hold sort list of questions
				QuestionsSet searchResults = new QuestionsSet();
				for (Question temp : sortedList) {
					searchResults.addQuestion(temp);
				}

				// Display results of updating similarity search in resultsBox
				// resultsBox.setText(searchResults.toString());
			}
		});

		// Button to submit an answer or question from the input fields
		submitButton.setOnAction(a -> {
			String titleInput = titleField.getText();
			String textInput = inputField.getText();

			// Check if titleInput is empty or null
			if (titleInput == null || titleInput.trim().isEmpty()) {
				errorLabel.setText("Error, question-title field is blank.");
				return;
			}

			// Check if textInput is empty or null
			if (textInput == null || textInput.trim().isEmpty()) {
				errorLabel.setText("Error, question-body field is blank.");
				return;
			}

			// Check if inputs are empty or null
			try {
				Question newSelection = qTable.getSelectionModel().getSelectedItem();

				if (newSelection != null) {
					// Update the currently selected question
					newSelection.setTitle(titleInput);
					newSelection.setText(textInput);
					databaseHelper.qaHelper.updateQuestion(newSelection);
				} else {
					// Register a new question in the database
					Question newQuestion = new Question(titleInput, textInput, databaseHelper.currentUser.getUserId());
					databaseHelper.qaHelper.registerQuestion(newQuestion);
				}

				// Clear input fields for new inputs
				titleField.clear();
				inputField.clear();

				// Retrieve an updated list of questions from the database
				questions = databaseHelper.qaHelper.getAllQuestions();

				// Refresh contents of tables manually
				questionObservableList.clear();
				questionObservableList.addAll(questions);
				qTable.setItems(questionObservableList);

			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("Error trying to register new question into database via submit button");
				;
				return;
			}

		});

		// Button to submit an answer or question from the input fields
		submitAnswerButton.setOnAction(a -> {
			String textInput = inputField.getText();

			// Check if textInput is empty or null
			if (textInput == null || textInput.trim().isEmpty()) {
				errorLabel.setText("Error, answer-body field is blank.");
				return;
			}

			// Check if inputs are empty or null
			try {
				Question newSelection = qTable.getSelectionModel().getSelectedItem();
				Answer answerSelection = aTable.getSelectionModel().getSelectedItem();

				if (newSelection == null && answerSelection != null) {
					// Update the currently selected answer
					answerSelection.setText(textInput);
					databaseHelper.qaHelper.updateAnswer(answerSelection);
				} else if (newSelection != null && answerSelection == null) {
					// Register a new question in the database
					Answer newAnswer = new Answer(textInput, databaseHelper.currentUser.getUserId());
					databaseHelper.qaHelper.registerAnswerWithQuestion(newAnswer, newSelection.getId());
				}

				// Clear input fields for new inputs
				titleField.clear();
				inputField.clear();

				// Retrieve an updated list of questions from the database
				question = qTable.getSelectionModel().getSelectedItem();
				questions = databaseHelper.qaHelper.getAllQuestions();

				// Refresh contents of tables manually
				questionObservableList.clear();
				questionObservableList.addAll(questions);
				qTable.setItems(questionObservableList);

			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("Error trying to register new question into database via submit button");
				;
				return;
			}

			// Update results table
			updateResultsTableForQuestion(question);

		});

		// Modify setOnAction for deleteButton to delete selected Question from Question
		// database
		deleteButton.setOnAction(a -> {
			try {
				Question newSelection = qTable.getSelectionModel().getSelectedItem();
				Answer answerSelection = aTable.getSelectionModel().getSelectedItem();

				if (newSelection != null && answerSelection == null) {
					// Delete selected question
					databaseHelper.qaHelper.deleteQuestion(newSelection.getId());
				} else if (newSelection == null && answerSelection != null) {
					// Delete selected answer
					databaseHelper.qaHelper.deleteAnswer(answerSelection.getId());
				}

				// Retrieve an updated list of questions from the database
				questions = databaseHelper.qaHelper.getAllQuestions();
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("Error trying update question object via getALLUsers() in qTable");
			}

			// Refresh contents of tables manually
			questionObservableList.clear();
			questionObservableList.addAll(questions);
			qTable.setItems(questionObservableList);

		});

		/*
		 * // Add listener to the resultsTable to allow dynamic selection of objects
		 * from // the table
		 * resultsTable.getSelectionModel().selectedItemProperty().addListener((obs,
		 * oldRow, newRow) -> { if (newRow != null) {
		 * 
		 * if (newRow.getType() == QATableRow.RowType.QUESTION) { // Question specific
		 * actions } else { // Answer specific actions } } });
		 */

		// Add listeners for the textArea title field
		titleField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
			if (isFocused) {
				qTable.getSelectionModel().clearSelection();
				aTable.getSelectionModel().clearSelection();
			}
		});

		// Add listeners for the textArea input field
		inputField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
			if (isFocused) {
				qTable.getSelectionModel().clearSelection();
				aTable.getSelectionModel().clearSelection();
			}
		});

		// Add listeners for table selections to read selected objects from tables
		qTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				// Clear selections on the other tables
				aTable.getSelectionModel().clearSelection();
				
				this.question = newSelection;

				// Update text for buttons
				submitButton.setText("Update Question");
				deleteButton.setText("Delete Question");

				// Update results table
				updateResultsTableForQuestion(newSelection);

			} else {
				// Update the text of the submitButton
				submitButton.setText("Submit Question");
			}
		});

		// Add listeners for table selections to read selected objects from tables
		aTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				// Clear selections on the other tables
				qTable.getSelectionModel().clearSelection();

				// Update text on buttons
				submitAnswerButton.setText("Update Answer");
				deleteButton.setText("Delete Answer");
			} else {
				// If nothing is selected on the answer table then...
				submitAnswerButton.setText("Submit Answer");
			}
		});

		// Use containers to position and hold many different UI components
		HBox hboxTop = new HBox(10, questionInputBox, answerDB);
		hboxTop.setAlignment(Pos.TOP_CENTER);

		VBox vboxTop = new VBox(10, hboxTop);
		vboxTop.setAlignment(Pos.TOP_CENTER);

		VBox vbox = new VBox(10, vboxTop, resultsTable);
		vbox.setAlignment(Pos.CENTER);

		VBox vbox1 = new VBox(10, vbox, quitButton);
		vbox1.setAlignment(Pos.CENTER);

		HBox hbox1 = new HBox(5, questionDB, vbox1);

		VBox vbox2 = new VBox(hbox1, errorLabel);

		StackPane root = new StackPane(vbox2);
		root.setStyle("-fx-background-color: derive(gray, 60%);");

		Scene scene = new Scene(root, 1900, 1000);

		resultsTable.prefWidthProperty().bind(hbox1.widthProperty());
		resultsTable.prefHeightProperty().bind(vbox1.heightProperty());
		contentColumn.prefWidthProperty().bind(vbox1.widthProperty().subtract(19));
		detailsColumn.prefWidthProperty().bind(questionDB.widthProperty().subtract(19));

		// Set the scene to primary stage
		primaryStage.setScene(scene);
		primaryStage.setTitle("");
		primaryStage.centerOnScreen();
		primaryStage.setMaximized(true);
		primaryStage.show();
	}

	private void updateResultsTableForQuestion(Question question) {
		// Clear the observable list
		resultsObservableList.clear();

		// Retrieve an updated answer list of answers related to selected question from
		// the qtable
		try {

			answers = databaseHelper.qaHelper.getAllAnswersForQuestion(question.getId());
			// question = qTable.getSelectionModel().getSelectedItem();

			// Put the selected question in the first row
			resultsObservableList.add(new QATableRow(QATableRow.RowType.QUESTION, question.toString(), null));

			// After that, if there are any, add each answer as its own row
			for (Answer answer : answers) {
				resultsObservableList.add(new QATableRow(QATableRow.RowType.ANSWER, answer.toString(), answer.getId(),
						answer.getAuthorId()));

				// Look for related answers to selected answer
				List<Answer> relatedAnswers = databaseHelper.qaHelper.getAllAnswersForAnswer(answer.getId());
				if (relatedAnswers != null && !relatedAnswers.isEmpty()) {
					for (Answer temp : relatedAnswers) {
						resultsObservableList.add(new QATableRow(QATableRow.RowType.ANSWER, temp.toString(),
								temp.getId(), temp.getAuthorId()));
					}
				}
			}

			// Check if selected question has a preferred answer and put that in row 2 if so
			if (question.getPreferredAnswer() > 0) {
				resultsObservableList.sort((row1, row2) -> {
					if (row1.getType() == QATableRow.RowType.QUESTION) {
						return -1;
					}
					if (row2.getType() == QATableRow.RowType.QUESTION) {
						return 1;
					}

					if (row1.getAnswerId() != null && row1.getAnswerId().equals(question.getPreferredAnswer())) {
						return -1;
					} else if (row2.getAnswerId() != null && row2.getAnswerId().equals(question.getPreferredAnswer())) {
						return 1;
					}

					return 0;
				});
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(
					"Error trying to .getAllAnswers() within qTable.getSelectionModel... on line 327 in StudentHomePage.java");
			return;
		}

		resultsTable.setItems(resultsObservableList);
		resultsTable.refresh();
	}

}
