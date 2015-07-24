package de.uni.bremen.monty.moco.util.astsearch;

import org.junit.Before;
import org.junit.Test;

public class SearchASTTest {

    @Before
    public void setUp() throws Exception {}

    
    @Test(expected = NullPointerException.class)
    public void testStreamNull() throws Exception {
        SearchAST.stream(null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testParentStreamNull() throws Exception {
        SearchAST.parentStream(null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testForNodeNull() throws Exception {
        SearchAST.forNode(null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testForParentNull() throws Exception {
        SearchAST.forParent(null);
    }
}
