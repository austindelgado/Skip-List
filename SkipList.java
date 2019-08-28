// Austin Delgado
// Skip List

import java.io.*;
import java.util.*;
import java.util.ArrayList;

class Node<AnyType>
{
	AnyType data;
	int height;
	ArrayList<Node<AnyType>> nextNodes = new ArrayList<>();

	// Creates head node
	Node(int height)
	{
		this.height = height;

		for (int i = 0; i < height; i++)
			nextNodes.add(null);
	}

	// Creates non head node
	Node(AnyType data, int height)
	{
		this.data = data;
		this.height = height;

		for (int i = 0; i < height; i++)
			nextNodes.add(null);
	}

	public AnyType value()
	{
		return this.data;
	}

	public int height()
	{
		height = nextNodes.size();
		return height;
	}

	public Node<AnyType> next(int level)
	{
		if (level < 0 || level > height - 1)
			return null;
		else
			return nextNodes.get(level);
	}

	public void setNext(int level, Node<AnyType> node)
	{
		nextNodes.set(level, node);
	}

	// Grows node. Used only on head node
	public void grow()
	{
		Node<AnyType> maybeGrow = new Node<AnyType>(1);
		Node<AnyType> lastGrown = new Node<AnyType>(1);
		int firstNeighbor = 0;
		boolean grew;

		// Head node always grows
		nextNodes.add(null);
		height++;

		// Loops through neighbors of previous height of head node and calls
		if (this.next(height - 2) != null)
		{
			maybeGrow = this.next(height - 2);
			lastGrown = maybeGrow;

			while (maybeGrow != null)
			{
				grew = maybeGrow.maybeGrow();
				// First neighbor that grew for head
				if (firstNeighbor == 0 && grew == true)
				{
					nextNodes.set(height - 1, maybeGrow);
					lastGrown = maybeGrow;
					firstNeighbor = 1;
				}
				else if (grew == true)
				{
					lastGrown.setNext(height - 1, maybeGrow);
					lastGrown = maybeGrow;
				}

				if (maybeGrow.next(height - 2) != null)
					maybeGrow = maybeGrow.next(height - 2);
				else
				{
					break;
				}
			}
		}
	}

	// Helper function of grow()
	public boolean maybeGrow()
	{
		double coinFlip = Math.random();
		// 50/50 chance the called node grows
		if (coinFlip < .5)
		{
			nextNodes.add(null);
			this.height++;
			return true;
		}
		return false;
	}

	public void trim(int height)
	{
		int trimHeight = height;

		while (this.height > trimHeight)
		{
			nextNodes.remove(height);
			this.height--;
		}
	}
}


public class SkipList<AnyType extends Comparable<AnyType>>
{
	Node<AnyType> skipListHead;
	int listHeight;
	int size;

	SkipList()
	{
		listHeight = 1;
		size = 0;

		skipListHead = new Node<AnyType>(1);
	}

	SkipList(int height)
	{
		this.listHeight = height;
		size = 0;

		skipListHead = new Node<AnyType>(height);
	}

	public int size()
	{
		return this.size;
	}

	public int height()
	{
		return this.listHeight;
	}

	public Node<AnyType> head()
	{
		return skipListHead;
	}

	// Calls insert but with random height based of max possible height
	public void insert(AnyType data)
	{
		insert(data, generateRandomHeight(getMaxHeight(listHeight, size + 1)));
	}

	public void insert(AnyType data, int height)
	{
		int newNodeHeight = height;
		AnyType newData = data;
		size++;

		// If the current head height is less than the max maxPossibleHeight it grows
		// Ignores cases where head is larger than maxPossibleHeight
		if (skipListHead.height() < (int)(Math.ceil(Math.log(size) / Math.log(2))))
		{
			skipListHead.grow();
			listHeight = skipListHead.height();
		}

		Node<AnyType> newNode = new Node<AnyType>(newData, newNodeHeight);
		Node<AnyType> currentNode = skipListHead;
		Node<AnyType> previousNode = skipListHead;
		Node<AnyType> updateNode = skipListHead;
		HashMap<Integer, Node<AnyType>> updateStorage= new HashMap<>();

		int levelCheck = skipListHead.height() - 1;

		while (levelCheck >= 0)
		{
			// Null value found
			if (currentNode.value() == null)
			{
				// First node!
				if (levelCheck == 0 &&  currentNode.next(levelCheck) == null)
				{
					for (int i = 0; i < newNodeHeight; i++)
					{
						skipListHead.setNext(i, newNode);
						if (updateStorage.containsKey(i))
						{
							updateNode = updateStorage.get(i);
							updateNode.setNext(i, newNode);
						}
					}
					break;
				}
				// Null neighbor
				else if (levelCheck != 0 && currentNode.next(levelCheck) == null)
				{
					updateStorage.put(levelCheck, currentNode);
					levelCheck--;
				}
				// Neighbor exists
				else if (currentNode.next(levelCheck) != null)
				{
					// Neighbor is smaller
					if (newData.compareTo(currentNode.next(levelCheck).value()) > 0)
					{
						previousNode = currentNode;
						currentNode = currentNode.next(levelCheck);
					}
					// Neighbor is larger
					else if (levelCheck != 0 && newData.compareTo(currentNode.next(levelCheck).value()) < 0)
					{
						updateStorage.put(levelCheck, currentNode);
						levelCheck--;
					}
					// Adding node in between two others
					else if (newData.compareTo(currentNode.next(levelCheck).value()) < 0 && levelCheck == 0)
					{
						for (int i = 0; i < newNodeHeight; i++)
						{
							if (currentNode.height() >= i + 1)
							{
								newNode.setNext(i, currentNode.next(i));
								currentNode.setNext(i, newNode);
							}
							else
							{
								if (updateStorage.containsKey(i) && i != 0)
								{
									updateNode = updateStorage.get(i);
									updateNode.setNext(i, newNode);
								}
							}
						}
						break;
					}
				}
			}
			else if (currentNode.value() != null)
			{
				// Neighbor exists
				if (currentNode.next(levelCheck) != null)
				{
					// Neighbor is greater than what we are inserting
					// but we are all the way at the bottom. Insert here
					if (newData.compareTo(currentNode.next(levelCheck).value()) < 0 && levelCheck == 0)
					{
						for (int i = 0; i < newNodeHeight; i++)
						{
							if (currentNode.height() >= i + 1)
							{
								newNode.setNext(i, currentNode.next(i));
								currentNode.setNext(i, newNode);
							}
							else
							{
								if (updateStorage.containsKey(i) && i != 0)
								{
									updateNode = updateStorage.get(i);
									updateNode.setNext(i, newNode);
								}
							}
						}
						break;
					}
					// Neighbor is less than value we're inserting
					// Move right
					else if (newData.compareTo(currentNode.next(levelCheck).value()) > 0)
					{
						previousNode = currentNode;
						currentNode = currentNode.next(levelCheck);
					}
					// Neighbor is greater than value we're inserting
					// Move down
					else if (newData.compareTo(currentNode.next(levelCheck).value()) < 0 && levelCheck != 0)
					{
						updateStorage.put(levelCheck, currentNode);
						levelCheck--;
					}
					else if (newData.compareTo(currentNode.next(levelCheck).value()) < 0 && levelCheck == 0)
					{
						// Adds a new node between current node and next node
						for (int i = 0; i < newNodeHeight; i++)
						{
							if (currentNode.height() >= i + 1)
							{
								newNode.setNext(i, currentNode.next(i));
								currentNode.setNext(i, newNode);
							}
							else
							{
								if (updateStorage.containsKey(i) && i != 0)
								{
									updateNode = updateStorage.get(i);
									updateNode.setNext(i, newNode);
								}
							}
						}
						break;
					}
				}
				// Neighbor is null
				else
				{
					if (levelCheck != 0)
					{
						// Dropping down because null neighbor
						updateStorage.put(levelCheck, currentNode);
						levelCheck--;
					}
					else if (newData.compareTo(currentNode.value()) > 0 && levelCheck == 0)
					{
						// Adds new biggest node to the end of the list
						for (int i = 0; i < newNodeHeight; i++)
						{
							if (currentNode.height() >= i + 1)
							{
								currentNode.setNext(i, newNode);
							}
							else
							{
								if (updateStorage.containsKey(i) && i != 0)
								{
									updateNode = updateStorage.get(i);
									//System.out.println("updateNode value: " + updateNode.value() + ", level: " + i);
									updateNode.setNext(i, newNode);
								}
							}
						}
						break;
					}
				}
			}
		}
	}

	public void delete(AnyType data)
	{

	}

	public boolean contains(AnyType data)
	{
		Node<AnyType> currentNode = skipListHead;
		AnyType searchData = data;
		int levelCheck = skipListHead.height() - 1;

		while (levelCheck >= 0)
		{
			// No neigbor
			// Move down
			if (currentNode.next(levelCheck).value() == null)
			{
				levelCheck--;
			}
			else if (currentNode.next(levelCheck).value() != null)
			{
				// Search number is larger than neighbor
				// Move right
				if (searchData.compareTo(currentNode.next(levelCheck).value()) > 0)
				{
					currentNode = currentNode.next(levelCheck);
				}
				// Search number is smaller than neighbor
				// Move down
				else if (searchData.compareTo(currentNode.next(levelCheck).value()) < 0)
				{
					levelCheck--;
				}
				else if (searchData.compareTo(currentNode.next(levelCheck).value()) == 0)
				{
					return true;
				}
				else if (levelCheck == 0 && currentNode.next(levelCheck) == null)
					return false;
			}
		}
		return false;
	}

	public Node<AnyType> get(AnyType data)
	{
		Node<AnyType> currentNode = skipListHead;
		AnyType searchData = data;
		int levelCheck = skipListHead.height() - 1;

		while (levelCheck >= 0)
		{
			// No neigbor
			// Move down
			if (currentNode.next(levelCheck).value() == null)
			{
				levelCheck--;
			}
			else if (currentNode.next(levelCheck).value() != null)
			{
				// Search number is larger than neighbor
				// Move right
				if (searchData.compareTo(currentNode.next(levelCheck).value()) > 0)
				{
					currentNode = currentNode.next(levelCheck);
				}
				// Search number is smaller than neighbor
				// Move down
				else if (searchData.compareTo(currentNode.next(levelCheck).value()) < 0)
				{
					levelCheck--;
				}
				else if (searchData.compareTo(currentNode.next(levelCheck).value()) == 0)
				{
					return currentNode.next(levelCheck);
				}
				else if (levelCheck == 0 && currentNode.next(levelCheck) == null)
					return null;
			}
		}
		return null;
	}

	// Returns greater of current headHeight or maxPossibleHeight list height
	private static int getMaxHeight(int headHeight, int size)
	{
		int maxPossibleHeight;
		if (size == 1)
			return 1;
		maxPossibleHeight = (int)(Math.ceil(Math.log(size) / Math.log(2)));

		if (headHeight > maxPossibleHeight)
			return headHeight;
		else
			return maxPossibleHeight;
	}

	// Returns random height less than or equal to maxHeight
	private static int generateRandomHeight(int maxHeight)
	{
		int currentHeight = 1;
		double coinFlip;
		// Continuosly flips coin until tails
		while (currentHeight < maxHeight)
		{
			coinFlip = Math.random();
			if (coinFlip < .5)
			{
				currentHeight++;
			}
			else
			{
				break;
			}
		}
		return currentHeight;
	}
}
