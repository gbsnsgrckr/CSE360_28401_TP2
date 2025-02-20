package databasePart1;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import application.Question;
import application.Answer;
import application.QuestionsSet;
import application.AnswersSet;
import tests.*;

import application.User;

// This is a database class used to create, read, update, and delete SQL databases among other managing functions in order
// to maintain a database of questions and answers and to perform various functions to them.

public class QAHelper1 {
	private DatabaseHelper databaseHelper;

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL = "jdbc:h2:~/QADatabase";

	// Database credentials
	static final String USER = "sa";
	static final String PASS = "";

	private Connection connection = null;
	private Statement statement = null;

	public QAHelper1(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	// Initialize connection to database
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);

			statement = connection.createStatement();
			// You can use this command to clear the Question/Answer database and restart
			// from fresh.
			// statement.execute("DROP ALL OBJECTS");
			// System.out.println("Database cleared successfully.");

			createTables();
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	// Create the tables that will be used to store the info
	public void createTables() throws SQLException {
		// Create the question database
		String questionTable = "CREATE TABLE IF NOT EXISTS cse360question ("
				+ "id INT AUTO_INCREMENT NOT NULL PRIMARY KEY, " + "title VARCHAR(255), " + "text TEXT DEFAULT NULL, "
				+ "author INT, " + "created_on DATETIME DEFAULT CURRENT_TIMESTAMP, "
				+ "updated_on DATETIME DEFAULT CURRENT_TIMESTAMP, " + "answer_id VARCHAR(255) DEFAULT NULL, "
				+ "preferred_answer INT DEFAULT NULL)";
//				+ "FOREIGN KEY (author) REFERENCES cse360users(id))";		// Currently not linked to user database
		statement.execute(questionTable);

		// Create the answer database
		String answerTable = "CREATE TABLE IF NOT EXISTS cse360answer ("
				+ "id INT AUTO_INCREMENT NOT NULL PRIMARY KEY, " + "text TEXT DEFAULT NULL, " + "author INT, "
				+ "created_on DATETIME DEFAULT CURRENT_TIMESTAMP, " + "updated_on DATETIME DEFAULT CURRENT_TIMESTAMP, "
				+ "answer_id VARCHAR(255) DEFAULT NULL)";
//				+ "FOREIGN KEY (author) REFERENCES cse360users(id))";		// Currently not linked to user database
		statement.execute(answerTable);
	}

	// Check if the database is empty - Only checks the question database at the
	// moment
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360question";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new question in the database.
	public void registerQuestion(Question question) throws SQLException {
		String insertQuestion = "INSERT INTO cse360question (title, text, author) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertQuestion)) {
			pstmt.setString(1, question.getTitle());
			pstmt.setString(2, question.getText());
			pstmt.setInt(3, question.getAuthorId());
			pstmt.executeUpdate();
		}
		System.out.println("Question registered successfully.");
	}

	// Registers a new answer in the database.
	public void registerAnswerWithQuestion(Answer answer, int questionID) throws SQLException {
		String insertAnswer = "INSERT INTO cse360answer (text, author) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertAnswer, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, answer.getText());
			pstmt.setInt(2, answer.getAuthorId());
			pstmt.executeUpdate();

			ResultSet newID = pstmt.getGeneratedKeys();
			if (newID.next()) {
				int answerID = newID.getInt(1);
				addRelationToQuestion(questionID, answerID);
			}

		}
		System.out.println("Answer registered successfully.");
	}
	
	// Registers a new answer in the database.
		public void registerAnswerWithAnswer(Answer answer, int relatedID) throws SQLException {
			String insertAnswer = "INSERT INTO cse360answer (text, author) VALUES (?, ?)";
			try (PreparedStatement pstmt = connection.prepareStatement(insertAnswer, Statement.RETURN_GENERATED_KEYS)) {
				pstmt.setString(1, answer.getText());
				pstmt.setInt(2, answer.getAuthorId());
				pstmt.executeUpdate();

				ResultSet newID = pstmt.getGeneratedKeys();
				if (newID.next()) {
					int answerID = newID.getInt(1);
					addRelationToQuestion(relatedID, answerID);
				}

			}
			System.out.println("Answer registered successfully.");
		}

	// Deletes a question row from the SQL table
	public boolean deleteQuestion(int id) {
		String query = "DELETE FROM cse360question AS c WHERE c.id = ?"; // delete the correct question row from
																			// database
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);

			// Check if any matches were found and deleted
			if (pstmt.executeUpdate() > 0) {
				System.out.println("DELETE-QUESTION: Question successfully deleted");
				// Remove answers related to question
				List<Answer> answers = getAllAnswersForQuestion(id);
				for (Answer answer : answers) {
					deleteAnswer(answer.getId());
				}
				return true;
			}
			System.out.println("DELETE-QUESTION: Question was not found");
			return false;
		} catch (SQLException e) {
			System.err.println("DELETE-QUESTION: SQL Error - " + e.getMessage());
			return false;
		}
	}

	// Deletes a question row from the SQL table
	public boolean deleteAnswer(int id) {
		String query = "DELETE FROM cse360answer AS c WHERE c.id = ?"; // delete the correct answer row from database
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);

			// Check if any matches were found and deleted
			if (pstmt.executeUpdate() > 0) {
				System.out.println("DELETE-ANSWER: Answer successfully deleted");
				return true;
			}
			System.out.println("DELETE-ANSWER: Answer was not found");
			return false;
		} catch (SQLException e) {
			System.err.println("DELETE-ANSWER: SQL Error - " + e.getMessage());
			return false;
		}
	}

	// Add a relation to the question database
	public void addRelationToQuestion(int questionID, int answerID) {
		String selectQuery = "SELECT answer_id FROM cse360question WHERE id = ?";
		String updateQuery = "UPDATE cse360question SET answer_id = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
			pstmt.setInt(1, questionID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				String answerIDs = rs.getString("answer_id");
				String newAnswerIDs;

				// Check if null or empty
				if (answerIDs == null || answerIDs.trim().isEmpty()) {
					newAnswerIDs = String.valueOf(answerID);
				} else {
					// If it is not empty or null, then add a comma and space
					newAnswerIDs = answerIDs + ", " + answerID;
				}

				try (PreparedStatement upstmt = connection.prepareStatement(updateQuery)) {
					upstmt.setString(1, newAnswerIDs);
					upstmt.setInt(2, questionID);
					upstmt.executeUpdate();
				}
			} else {
				System.err.println("Could not find a question with id: " + questionID);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage() + "\nERROR IN ADD-RELATION METHOD");
		}
	}
	
	// Add a relation to the answer database
		public void addRelationToAnswer(int answerID, int relatedID) {
			String selectQuery = "SELECT answer_id FROM cse360answer WHERE id = ?";
			String updateQuery = "UPDATE cse360answer SET answer_id = ? WHERE id = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
				pstmt.setInt(1, answerID);
				ResultSet rs = pstmt.executeQuery();

				if (rs.next()) {
					String answerIDs = rs.getString("answer_id");
					String newAnswerIDs;

					// Check if null or empty
					if (answerIDs == null || answerIDs.trim().isEmpty()) {
						newAnswerIDs = String.valueOf(relatedID);
					} else {
						// If it is not empty or null, then add a comma and space
						newAnswerIDs = answerIDs + ", " + relatedID;
					}

					try (PreparedStatement upstmt = connection.prepareStatement(updateQuery)) {
						upstmt.setString(1, newAnswerIDs);
						upstmt.setInt(2, answerID);
						upstmt.executeUpdate();
					}
				} else {
					System.err.println("Could not find an answer with id: " + answerID);
				}
			} catch (SQLException e) {
				System.err.println(e.getMessage() + "\nERROR IN ADD-RELATION METHOD");
			}
		}

	// Delete a relation from the relation database
	public boolean deleteRelation(int questionID, int answerID) {
		String selectQuery = "SELECT answer_id FROM cse360question WHERE id = ?";
		String updateQuery = "UPDATE cse360question SET answer_id = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
			pstmt.setInt(1, questionID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				String answerIDs = rs.getString("answer_id");

				// Check if null or empty
				if (answerIDs == null || answerIDs.trim().isEmpty()) {
					System.err.println("Error: There are no answers related to this question.");
					return false;
				}

				// convert comma separated list into an array
				String[] answerIdArray = answerIDs.split(",\\s");
				List<String> answerIdList = new ArrayList<>(Arrays.asList(answerIdArray));

				boolean idFound = answerIdList.remove(String.valueOf(answerID));
				if (!idFound) {
					System.out.println("Could not find answer id relation in provided question id: " + questionID);
					return false;
				}

				// Put list back into comma separated list
				String newAnswerIDs = String.join(", ", answerIdList);

				try (PreparedStatement upstmt = connection.prepareStatement(updateQuery)) {
					upstmt.setString(1, newAnswerIDs.isEmpty() ? null : newAnswerIDs);
					upstmt.setInt(2, questionID);
					upstmt.executeUpdate();
					System.out.println("Answer ID: " + answerID
							+ " was successfully removed from relation with question id: " + questionID);
					return true;
				}
			} else {
				System.err.println("Could not find a question with id: " + questionID);
				return false;
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage() + "\nERROR IN DELETE-RELATION METHOD");
			return false;
		}
	}

	// Get a question object with a provided question id
	public Question getQuestion(Integer questionID) throws SQLException {
		// Search the question database for a match to the question id
		String query = "SELECT * FROM cse360question AS c WHERE c.id = ?	";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title"); // for each row, get all of the user attributes
				String text = rs.getString("text");
				int authorId = rs.getInt("author");
				Timestamp created = rs.getTimestamp("created_On");
				// Convert to LocalDateTime format
				LocalDateTime createdOn = created != null ? created.toLocalDateTime() : null;
				Timestamp updated = rs.getTimestamp("updated_On");
				// Convert to LocalDateTime format
				LocalDateTime updatedOn = updated != null ? updated.toLocalDateTime() : null;

				List<String> comp = textDeserial(text);

				int preferredAnswer = rs.getInt("preferred_answer");

				User author = databaseHelper.getUser(authorId);

				// Create a new question object with the pulled info
				Question question = new Question(id, title, text, authorId, createdOn, updatedOn, comp, preferredAnswer,
						author);

				// Return the question object
				return question;
			}
		}
		return null;
	}

	// Get an answer object with a provided answer id
	public Answer getAnswer(Integer answerID) throws SQLException {
		// Search the answer database for a match to the answer id
		String query = "SELECT * FROM cse360answer AS c WHERE c.id = ?	";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, answerID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				int id = rs.getInt("id");
				String text = rs.getString("text");
				int authorId = rs.getInt("author");
				Timestamp created = rs.getTimestamp("created_On");
				// Convert to LocalDateTime format
				LocalDateTime createdOn = created != null ? created.toLocalDateTime() : null;
				Timestamp updated = rs.getTimestamp("updated_On");
				// Convert to LocalDateTime format
				LocalDateTime updatedOn = updated != null ? updated.toLocalDateTime() : null;

				User author = databaseHelper.getUser(authorId);

				// Create a new answer object with the pulled info
				Answer answer = new Answer(id, text, authorId, createdOn, updatedOn, author);

				// Return the answer object
				return answer;
			}
		}
		return null;
	}

	// Retrieve all questions from the question database
	public List<Question> getAllQuestions() throws SQLException {
		String query = "SELECT * FROM cse360question"; // selecting all of the rows in the database
		List<Question> questions = new ArrayList<>();

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title"); // for each row, get all of the user attributes
				String text = rs.getString("text");
				int authorId = rs.getInt("author");
				Timestamp created = rs.getTimestamp("created_On");
				// Convert to LocalDateTime format
				LocalDateTime createdOn = created != null ? created.toLocalDateTime() : null;
				Timestamp updated = rs.getTimestamp("updated_On");
				// Convert to LocalDateTime format
				LocalDateTime updatedOn = updated != null ? updated.toLocalDateTime() : null;
				int preferredAnswer = rs.getInt("preferred_answer");

				List<String> comp = textDeserial(text);

				User author = databaseHelper.getUser(authorId);

				// Create a new question object with the pulled info
				Question question = new Question(id, title, text, authorId, createdOn, updatedOn, comp, preferredAnswer,
						author);

				// Add question object to the list questions
				questions.add(question);
			}
		}
		// Return the assembled list of question objects
		return questions;
	}

	public List<Question> getAllUnansweredQuestions() throws SQLException {
		String query = "SELECT * FROM cse360question WHERE answer_id IS NULL OR answer_id = ''"; // selecting all of the
																									// rows in the
																									// database
		List<Question> questions = new ArrayList<>(); // that don't have an answer id

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title"); // for each row, get all of the user attributes
				String text = rs.getString("text");
				int authorId = rs.getInt("author");
				Timestamp created = rs.getTimestamp("created_On");
				// Convert to LocalDateTime format
				LocalDateTime createdOn = created != null ? created.toLocalDateTime() : null;
				Timestamp updated = rs.getTimestamp("updated_On");
				// Convert to LocalDateTime format
				LocalDateTime updatedOn = updated != null ? updated.toLocalDateTime() : null;
				int preferredAnswer = rs.getInt("preferred_answer");

				List<String> comp = textDeserial(text);

				User author = databaseHelper.getUser(authorId);

				// Create a new question object with the pulled info
				Question question = new Question(id, title, text, authorId, createdOn, updatedOn, comp, preferredAnswer,
						author);

				// Add question object to the list questions
				questions.add(question);
			}
		}
		// Return the assembled list of question objects
		return questions;
	}

	public List<Question> getAllAnsweredQuestions() throws SQLException {
		String query = "SELECT * FROM cse360question WHERE answer_id IS NOT NULL AND answer_id <> ''"; // selecting all
																										// of the rows
																										// in the
																										// database
		List<Question> questions = new ArrayList<>(); // that don't have an answer id

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title"); // for each row, get all of the user attributes
				String text = rs.getString("text");
				int authorId = rs.getInt("author");
				Timestamp created = rs.getTimestamp("created_On");
				// Convert to LocalDateTime format
				LocalDateTime createdOn = created != null ? created.toLocalDateTime() : null;
				Timestamp updated = rs.getTimestamp("updated_On");
				// Convert to LocalDateTime format
				LocalDateTime updatedOn = updated != null ? updated.toLocalDateTime() : null;
				int preferredAnswer = rs.getInt("preferred_answer");

				List<String> comp = textDeserial(text);

				User author = databaseHelper.getUser(authorId);

				// Create a new question object with the pulled info
				Question question = new Question(id, title, text, authorId, createdOn, updatedOn, comp, preferredAnswer,
						author);

				// Add question object to the list questions
				questions.add(question);
			}
		}
		// Return the assembled list of question objects
		return questions;
	}

	// Retrieve all answers from the answer database
	public List<Answer> getAllAnswers() throws SQLException {
		String query = "SELECT * FROM cse360answer"; // selecting all of the rows in the database
		List<Answer> answers = new ArrayList<>();

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				// Pull the info from that row for one answer object
				int id = rs.getInt("id");
				String text = rs.getString("text");
				int authorId = rs.getInt("author");
				Timestamp created = rs.getTimestamp("created_On");
				// Convert to LocalDateTime format
				LocalDateTime createdOn = created != null ? created.toLocalDateTime() : null;
				Timestamp updated = rs.getTimestamp("updated_On");
				// Convert to LocalDateTime format
				LocalDateTime updatedOn = updated != null ? updated.toLocalDateTime() : null;

				User author = databaseHelper.getUser(authorId);

				// Create a new answer object with the pulled info
				Answer answer = new Answer(id, text, authorId, createdOn, updatedOn, author);

				// Add the new answer object to the list of answer objects
				answers.add(answer);
			}
		}
		// Return the list of answer objects
		return answers;
	}

	// Retrieve all of the answers that are associated with a given question id from
	// the question database
	public List<Answer> getAllAnswersForQuestion(int questionID) throws SQLException {
		String query = "SELECT answer_id FROM cse360question WHERE id = ?";
		List<Answer> answers = new ArrayList<>();

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				String answerIDs = rs.getString("answer_id");

				if (answerIDs == null || answerIDs.trim().isEmpty()) {
					System.err.println("Error: There are no answers related to this question.");
					return answers;
				}

				// convert comma separated list into an array
				String[] answerIdArray = answerIDs.split(",\\s");

				String temp = String.join(",", Collections.nCopies(answerIdArray.length, "?"));
				String newQuery = "SELECT id, text, author, created_On, updated_On FROM cse360answer WHERE id IN ("
						+ temp + ")";
				try (PreparedStatement upstmt = connection.prepareStatement(newQuery)) {

					for (int i = 0; i < answerIdArray.length; i++) {
						upstmt.setInt(i + 1, Integer.parseInt(answerIdArray[i].trim()));
					}

					ResultSet newRs = upstmt.executeQuery();

					while (newRs.next()) {
						int id = newRs.getInt("id");
						String text = newRs.getString("text");
						int author = newRs.getInt("author");
						Timestamp created = newRs.getTimestamp("created_On");
						// Convert to LocalDateTime format
						LocalDateTime createdOn = created != null ? created.toLocalDateTime() : null;
						Timestamp updated = newRs.getTimestamp("updated_On");
						// Convert to LocalDateTime format
						LocalDateTime updatedOn = updated != null ? updated.toLocalDateTime() : null;

						// Create a new answer object with the pulled info
						Answer answer = new Answer(id, text, author, createdOn, updatedOn);

						// Add the new answer object to the list of answer objects
						answers.add(answer);
					}
				}
			} else {
				System.err.println("Error. Question id, " + questionID + ", was not found");
			}
		}
		// Return the list of answer objects
		return answers;
	}

	// Retrieve all of the answers that are associated with a given answer id from
	// the answer database
	public List<Answer> getAllAnswersForAnswer(int answerID) throws SQLException {
		String query = "SELECT answer_id FROM cse360answer WHERE id = ?";
		List<Answer> answers = new ArrayList<>();

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, answerID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				String answerIDs = rs.getString("answer_id");

				if (answerIDs == null || answerIDs.trim().isEmpty()) {
					System.err.println("Error: There are no answers related to this answer.");
					return answers;
				}

				// convert comma separated list into an array
				String[] answerIdArray = answerIDs.split(",\\s");

				String temp = String.join(",", Collections.nCopies(answerIdArray.length, "?"));
				String newQuery = "SELECT id, text, author, created_On, updated_On FROM cse360answer WHERE id IN ("
						+ temp + ")";
				try (PreparedStatement upstmt = connection.prepareStatement(newQuery)) {

					for (int i = 0; i < answerIdArray.length; i++) {
						upstmt.setInt(i + 1, Integer.parseInt(answerIdArray[i].trim()));
					}

					ResultSet newRs = upstmt.executeQuery();

					while (newRs.next()) {
						int id = newRs.getInt("id");
						String text = newRs.getString("text");
						int author = newRs.getInt("author");
						Timestamp created = newRs.getTimestamp("created_On");
						// Convert to LocalDateTime format
						LocalDateTime createdOn = created != null ? created.toLocalDateTime() : null;
						Timestamp updated = newRs.getTimestamp("updated_On");
						// Convert to LocalDateTime format
						LocalDateTime updatedOn = updated != null ? updated.toLocalDateTime() : null;

						// Create a new answer object with the pulled info
						Answer answer = new Answer(id, text, author, createdOn, updatedOn);

						// Add the new answer object to the list of answer objects
						answers.add(answer);
					}
				}
			} else {
				System.err.println("Error. Answer id, " + answerID + ", was not found");
			}
		}
		// Return the list of answer objects
		return answers;
	}

	// Method used to convert the text of a question into a list of unique words
	// without special characters for comparison to others
	public List<String> textDeserial(String text) {
		if (text == null || text.isEmpty()) {
			// If empty return new empty list
			return new ArrayList<>();
		} else {
			// Clean string of all but alpha-numeric characters and spaces
			String cleanText = text.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();

			// Split the string using the spaces
			String[] wordsArray = cleanText.split("\\s+");

			// Use set to get rid of any duplicate words in the set
			Set<String> uniqueText = new HashSet<>(Arrays.asList(wordsArray));

			// Convert back to list and return
			return new ArrayList<>(uniqueText);
		}
	}

	// Update the contents of a question object with those of pass question object
	public void updateQuestion(Question question) {
		String query = "UPDATE cse360question Set title = ?, text = ?, updated_on = CURRENT_TIMESTAMP WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, question.getTitle());
			pstmt.setString(2, question.getText());
			pstmt.setInt(3, question.getId());

			int updated = pstmt.executeUpdate();
			// Check if any changes were made
			if (updated > 0) {
				System.out.println("Question has been updated."); // Debug
			} else {
				System.out.println("No matching question was found."); // Debug
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error trying to update question in updateQuestion method.");
		}
	}

	// Update the contents of a answer object with those of pass answer object
	public void updateAnswer(Answer answer) {
		String query = "UPDATE cse360answer Set text = ?, updated_on = CURRENT_TIMESTAMP WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, answer.getText());
			pstmt.setInt(2, answer.getId());

			int updated = pstmt.executeUpdate();
			// Check if any changes were made
			if (updated > 0) {
				System.out.println("Answer has been updated."); // Debug
			} else {
				System.out.println("No matching answer was found."); // Debug
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error trying to update question in updateAnswer method.");
		}
	}
}
