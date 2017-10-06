/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 KuiGang Wang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lt.compiler.err_rec;

import lt.compiler.ErrorManager;
import lt.compiler.IndentScanner;
import lt.compiler.Properties;
import lt.compiler.SyntaxException;
import lt.compiler.lexical.Element;
import lt.compiler.lexical.ElementStartNode;
import lt.compiler.lexical.EndingNode;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * error recovery
 */
public class TestScannerErrorRecovery {
        private ElementStartNode scan(String words, ErrorManager errorManager) throws IOException, SyntaxException {
                IndentScanner scanner = new IndentScanner("test.lt", new StringReader(words), new Properties(), errorManager);
                return scanner.scan();
        }

        @Test
        public void testUnknownToken() throws Exception {
                ErrorManager manager = new ErrorManager(false);
                manager.out = ErrorManager.Out.allNull();

                ElementStartNode root = scan("" +
                        "a ? = 1"
                /*         ^unknown token ? */
                        , manager);

                assertEquals(1, manager.errorList.size());
                ErrorManager.CompilingError err = manager.errorList.get(0);
                assertEquals(ErrorManager.CompilingError.UnknownToken, err.type);
                assertEquals(1, err.lineCol.line);
                assertEquals(3, err.lineCol.column);

                Element a = (Element) root.getLinkedNode();
                Element eq = (Element) a.next();
                Element one = (Element) eq.next();

                assertEquals("a", a.getContent());
                assertEquals("=", eq.getContent());
                assertEquals("1", one.getContent());
                assertNull(one.next());
        }

        @Test
        public void testUnexpectedToken_Pair_End() throws Exception {
                ErrorManager manager = new ErrorManager(false);
                manager.out = ErrorManager.Out.allNull();

                ElementStartNode root = scan("" +
                        "['id':1}"
                /*              ^unknown token ? */
                        , manager);

                assertEquals(1, manager.errorList.size());
                ErrorManager.CompilingError err = manager.errorList.get(0);
                assertEquals(ErrorManager.CompilingError.UnexpectedToken, err.type);
                assertEquals(1, err.lineCol.line);
                assertEquals(8, err.lineCol.column);

                Element _b1_ = (Element) root.getLinkedNode();
                ElementStartNode start = (ElementStartNode) _b1_.next();
                EndingNode end = (EndingNode) start.next();
                Element _b2_ = (Element) end.next();
                assertNull(_b2_.next());

                Element id = (Element) start.getLinkedNode();
                Element colon = (Element) id.next();
                Element one = (Element) colon.next();
                assertNull(one.next());

                assertEquals("[", _b1_.getContent());
                assertEquals("]", _b2_.getContent());
                assertEquals("'id'", id.getContent());
                assertEquals(":", colon.getContent());
                assertEquals("1", one.getContent());
        }
}
