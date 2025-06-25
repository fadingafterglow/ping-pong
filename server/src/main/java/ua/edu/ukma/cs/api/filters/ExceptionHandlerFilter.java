package ua.edu.ukma.cs.api.filters;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import ua.edu.ukma.cs.exception.ForbiddenException;
import ua.edu.ukma.cs.exception.NotFoundException;
import ua.edu.ukma.cs.exception.ValidationException;

import java.io.IOException;

/**
 * Should be registered first in the pipeline
 */
public class ExceptionHandlerFilter extends Filter {
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
            chain.doFilter(exchange);
        } catch (NotFoundException e) {
            exchange.sendResponseHeaders(404, 0);
        } catch (ForbiddenException e) {
            exchange.sendResponseHeaders(403, 0);
        } catch (ValidationException e) {
            exchange.sendResponseHeaders(400, 0);
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
        } finally {
            exchange.close();
        }
    }

    @Override
    public String description() {
        return "Handles exceptions returning corresponding error codes";
    }
}
