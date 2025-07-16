package com.example.demo.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileNameUtilTest {

	String url = "bucketName/f816b17b-ffbb-4a90-82f3-3c5c4f4da27f/D2/complaint_ack/748ee734-5d5d-41bf-ae5e-921f9fcaf99b/1752563550915.txt";

	@BeforeEach
	void setUp() throws Exception {

	}

	@Test
	void testRemovePrefix() {
		String result = FileNameUtil.removePrefix(url, false);
		assertEquals(
				"f816b17b-ffbb-4a90-82f3-3c5c4f4da27f/D2/complaint_ack/748ee734-5d5d-41bf-ae5e-921f9fcaf99b/1752563550915.txt",
				result);
	}

	@Test
	void testGetParentPath() {
		String result = FileNameUtil.getParentPath(url, false);
		assertEquals(
				"bucketName/f816b17b-ffbb-4a90-82f3-3c5c4f4da27f/D2/complaint_ack/748ee734-5d5d-41bf-ae5e-921f9fcaf99b",
				result);
	}

	@Test
	void testRemoveSegment() {
		String result = FileNameUtil.removeSegment(url, "f816b17b-ffbb-4a90-82f3-3c5c4f4da27f");
		assertEquals("bucketName/D2/complaint_ack/748ee734-5d5d-41bf-ae5e-921f9fcaf99b/1752563550915.txt", result);
	}

	@Test
	void testReplaceSegment() {
		String result = FileNameUtil.replaceSegment(url, "f816b17b-ffbb-4a90-82f3-3c5c4f4da27f",
				"8214eea2-332f-4cdb-b51f-d3517b4967f0");
		assertEquals(
				"bucketName/8214eea2-332f-4cdb-b51f-d3517b4967f0/D2/complaint_ack/748ee734-5d5d-41bf-ae5e-921f9fcaf99b/1752563550915.txt",
				result);

	}

	@Test
	void testCheckIndex() {
		Map<Integer, String> indexMap = FileNameUtil.checkIndex(url);
		System.out.println("indexMap:" + indexMap);
		assertNotNull(indexMap);
	}

	@Test
	void testReplaceSegmentsByIndex() {
		Map<Integer, String> indexMap = FileNameUtil.checkIndex(url);
		indexMap.put(0, "sqms");
		String result = FileNameUtil.replaceSegmentsByIndex(url, indexMap);
		assertEquals(
				"sqms/f816b17b-ffbb-4a90-82f3-3c5c4f4da27f/D2/complaint_ack/748ee734-5d5d-41bf-ae5e-921f9fcaf99b/1752563550915.txt",
				result);
	}

	@Test
	void testBuildPathFromMap() {
		Map<Integer, String> map = Map.of(0, "sqms", 1, "f816b17b-ffbb-4a90-82f3-3c5c4f4da27f", 2, "D2", 3,
				"complaint_ack", 4, "748ee734-5d5d-41bf-ae5e-921f9fcaf99b", 5, "1752563550915.txt");
		String result = FileNameUtil.buildPathFromMap(map);
		assertEquals(
				"sqms/f816b17b-ffbb-4a90-82f3-3c5c4f4da27f/D2/complaint_ack/748ee734-5d5d-41bf-ae5e-921f9fcaf99b/1752563550915.txt",
				result);
	}

	@Test
	void testGetSegmentAtIndex() {
		String result = FileNameUtil.getSegmentAtIndex(url, 1);
		assertEquals(
				"f816b17b-ffbb-4a90-82f3-3c5c4f4da27f",
				result);
	}

}
