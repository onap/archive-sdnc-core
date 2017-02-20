/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.sli;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.openecomp.sdnc.sli.ExprGrammarParser.AddExprContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.AtomContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.CompareExprContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.ConstantContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.ExprContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.FuncExprContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.MultExprContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.ParenExprContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.RelExprContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.VariableContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.VariableLeadContext;
import org.openecomp.sdnc.sli.ExprGrammarParser.VariableTermContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcLogicExprListener extends ExprGrammarBaseListener 
{




	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicExprListener.class);
	
	private SvcLogicExpression curExpr;
	private SvcLogicExpression topExpr;
	private LinkedList<SvcLogicExpression> exprStack;
	
	public SvcLogicExprListener()
	{
		exprStack = new LinkedList<SvcLogicExpression>();
	}
	
	public SvcLogicExpression getParsedExpr()
	{
		return(curExpr);
	}

	private void pushOperand(SvcLogicExpression operand)
	{
		if (curExpr == null)
		{
			curExpr = operand;
		}
		else
		{
			curExpr.addOperand(operand);
		}
	}
	
	private void pushExpr(SvcLogicExpression expr)
	{
		LOG.debug("Pushing expression ["+expr.getClass().getName()+"]");
		if (curExpr != null)
		{
			exprStack.push(curExpr);
		}
		curExpr = expr;
	}
	
	private void popExpr()
	{
		if (exprStack.isEmpty())
		{
			LOG.debug("Popping last expression");
			topExpr = curExpr;
		}
		else
		{
			SvcLogicExpression lastExpr = curExpr;
			curExpr = exprStack.pop();
			curExpr.addOperand(lastExpr);
			LOG.debug("New curExpr is ["+curExpr.getClass().getName()+"]");
		}
		
	}
	
	@Override
	public void enterAtom(AtomContext ctx) {
		
		String atomText = ctx.getText();
		
		LOG.debug("enterAtom: text = "+atomText);

		
		SvcLogicAtom newAtom = new SvcLogicAtom(atomText);
		
		pushExpr(newAtom);
	}


	@Override
	public void enterMultExpr(MultExprContext ctx) {
		LOG.debug("enterMultExpr: text = "+ctx.getText());
		
		SvcLogicBinaryExpression curBinExpr = new SvcLogicBinaryExpression();
		pushExpr(curBinExpr);
		
		List<TerminalNode> opList = ctx.MULTOP();
		
		for (TerminalNode nd : opList)
		{
			LOG.debug("enterMultExpr: operator - "+nd.getText());
			curBinExpr.addOperator(nd.getText());
		}

	}

	@Override
	public void exitMultExpr(MultExprContext ctx) {

		LOG.debug("exitMultExpr: text = "+ctx.getText());

		popExpr();
		
	}

	@Override
	public void exitAtom(AtomContext ctx) {
		LOG.debug("exitAtom: text = "+ctx.getText());
		popExpr();
	}

	@Override
	public void enterAddExpr(AddExprContext ctx) {
		LOG.debug("enterAddExpr: text = "+ctx.getText());
		List<TerminalNode> opList = ctx.ADDOP();
		

		SvcLogicBinaryExpression curBinExpr = new SvcLogicBinaryExpression();
		pushExpr(curBinExpr);

		
		for (TerminalNode nd : opList)
		{
			LOG.debug("enterAddExpr: operator - "+nd.getText());
			curBinExpr.addOperator(nd.getText());
		}
		
	}

	@Override
	public void exitAddExpr(AddExprContext ctx) {
		LOG.debug("exitAddExpr: text = "+ctx.getText());
		
		popExpr();
	}

	@Override
	public void enterFuncExpr(FuncExprContext ctx) {
		LOG.debug("enterFuncExpr: text = "+ctx.getText());
		LOG.debug("enterFuncExpr - IDENTIFIER : "+ctx.IDENTIFIER().getText());
		
		for (ExprContext expr: ctx.expr())
		{
			LOG.debug("enterFuncExpr - expr = "+expr.getText());
		}
		

		pushExpr(new SvcLogicFunctionCall(ctx.IDENTIFIER().getText()));
	}

	@Override
	public void exitFuncExpr(FuncExprContext ctx) {
		LOG.debug("exitFuncExpr: text = "+ctx.getText());
		
		popExpr();
	}

	@Override
	public void enterParenExpr(ParenExprContext ctx) {
		LOG.debug("enterParenExpr: text = "+ctx.getText());
		LOG.debug("enterParenExpr: expr = "+ctx.expr().getText());
	}

	@Override
	public void exitParenExpr(ParenExprContext ctx) {
		LOG.debug("exitParenExpr: text = "+ctx.getText());
	}

	@Override
	public void enterRelExpr(RelExprContext ctx) {
		LOG.debug("enterRelExpr: text = "+ctx.getText());
		
		List<TerminalNode> opList = ctx.RELOP();
		

		SvcLogicBinaryExpression curBinExpr = new SvcLogicBinaryExpression();
		pushExpr(curBinExpr);

		
		for (TerminalNode nd : opList)
		{
			LOG.debug("enterRelExpr: operator - "+nd.getText());
			curBinExpr.addOperator(nd.getText());
		}
		
	}

	@Override
	public void exitRelExpr(RelExprContext ctx) {
		LOG.debug("exitRelExpr: text = "+ctx.getText());
		
		popExpr();
	}

	@Override
	public void enterCompareExpr(CompareExprContext ctx) {
		LOG.debug("enterCompareExpr: text = "+ctx.getText());
		
		TerminalNode nd = ctx.COMPAREOP();

		SvcLogicBinaryExpression curBinExpr = new SvcLogicBinaryExpression();
		pushExpr(curBinExpr);

		LOG.debug("enterCompareExpr: operator - "+nd.getText());
		curBinExpr.addOperator(nd.getText());

	}

	@Override
	public void exitCompareExpr(CompareExprContext ctx) {
		LOG.debug("exitCompareExpr : text = "+ctx.getText());
		
		popExpr();
	}


	
	@Override 
	public void enterConstant(ConstantContext ctx) {
		LOG.debug("enterConstant: text = "+ctx.getText());
	}

	@Override
	public void exitConstant(ConstantContext ctx) {
		LOG.debug("exitConstant: text = "+ctx.getText());
	}


	@Override
	public void enterVariable(VariableContext ctx) {
		LOG.debug("enterVariable: text = "+ctx.getText());
		
		
	}

	@Override
	public void exitVariable(VariableContext ctx) {
		LOG.debug("exitVariable: text ="+ctx.getText());
		
	}
	

	@Override
	public void enterVariableLead(VariableLeadContext ctx) {

		LOG.debug("enterVariableLead: text ="+ctx.getText());
		

	}

	@Override
	public void exitVariableLead(VariableLeadContext ctx) {

		LOG.debug("exitVariableLead: text ="+ctx.getText());
	}

	@Override
	public void enterVariableTerm(VariableTermContext ctx) {
		LOG.debug("enterVariableTerm: text ="+ctx.getText());
		
		String name = ctx.getText();
		
		int subscrStart = name.indexOf("[");
		if (subscrStart > -1)
		{
			name = name.substring(0, subscrStart);
		}
		SvcLogicVariableTerm vterm = new SvcLogicVariableTerm(name);
		pushExpr(vterm);
	}

	@Override
	public void exitVariableTerm(VariableTermContext ctx) {
		LOG.debug("exitVariableTerm: text="+ctx.getText());
		popExpr();
	}
}
